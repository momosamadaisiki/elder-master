package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.dto.AiChatMessage;
import com.situ.elder.pojo.entity.*;
import com.situ.elder.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 康养小智实现：
 * 1) 从系统读取当前老人健康档案摘要（档案/标签/护理任务/体检记录），作为上下文；
 * 2) 以 OpenAI 兼容协议直调阿里云百炼（复用 application.yml 中 spring.ai.dashscope 的 key 与 model）。
 */
@Service
public class AiChatServiceImpl implements IAiChatService {

    private static final String PROMPT_PATH = "ai/kangyang-xiaozhi-system.md";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_HISTORY = 12;
    private static final int MAX_MSG_CHARS = 2000;
    private static final int MAX_TOTAL_CHARS = 8000;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${spring.ai.dashscope.api-key:}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.options.model:qwen-plus}")
    private String model;

    @Value("${ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Autowired
    private IElderService elderService;
    @Autowired
    private ITagService tagService;
    @Autowired
    private IElderTagService elderTagService;
    @Autowired
    private ICareTaskService careTaskService;
    @Autowired
    private ICarePlanService carePlanService;
    @Autowired
    private ICareLevelService careLevelService;
    @Autowired
    private IExamAppointmentService examAppointmentService;
    @Autowired
    private IExamAppointmentItemService examAppointmentItemService;

    @Override
    public String chat(Long elderId, List<AiChatMessage> messages) {
        if (ObjectUtils.isEmpty(apiKey)) {
            throw new ServiceException("AI 助手暂未配置，请联系管理员开启");
        }
        if (ObjectUtils.isEmpty(messages)) {
            throw new ServiceException("消息不能为空");
        }

        String systemText = buildSystemPrompt(elderId);
        List<AiChatMessage> history = sanitize(messages);
        if (history.isEmpty()) {
            throw new ServiceException("消息不能为空");
        }

        try {
            return callDashScope(systemText, history);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("AI 服务暂时不可用，请稍后再试");
        }
    }

    /** 组装 system：康养小智角色设定 + 当前老人健康档案摘要 */
    private String buildSystemPrompt(Long elderId) {
        String basePrompt = loadPrompt();
        String context = buildHealthContext(elderId);
        if (context.isBlank()) {
            return basePrompt;
        }
        return basePrompt + "\n\n【当前老人健康档案摘要（供你参考；只用于辅助判断，注意不确定性与隐私，不要整段复述给老人/家属）】\n" + context;
    }

    private String loadPrompt() {
        try {
            return StreamUtils.copyToString(new ClassPathResource(PROMPT_PATH).getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new ServiceException("AI 角色配置加载失败");
        }
    }

    /**
     * 只读汇总当前老人的健康上下文。不包含密码/身份证号等敏感字段。
     */
    private String buildHealthContext(Long elderId) {
        Elder elder = elderService.getById(elderId);
        if (elder == null) {
            return "";
        }
        List<String> lines = new ArrayList<>();

        //1) 基本信息
        Integer age = null;
        if (elder.getBirthday() != null) {
            age = Period.between(elder.getBirthday().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears();
        }
        lines.add("老人档案：" + safe(elder.getName()) + "，年龄约" + (age == null ? "未知" : age + "岁")
                + "，状态：" + elderStatusText(elder.getStatus())
                + (ObjectUtils.isEmpty(elder.getAddress()) ? "" : "，住址：" + elder.getAddress())
                + (ObjectUtils.isEmpty(elder.getRemark()) ? "" : "，备注：" + elder.getRemark()));

        //2) 标签（慢病等信息）
        List<Long> tagIds = elderTagService.list(new LambdaQueryWrapper<ElderTag>().eq(ElderTag::getElderId, elderId))
                .stream().map(ElderTag::getTagId).toList();
        if (!tagIds.isEmpty()) {
            String tagNames = tagService.listByIds(tagIds).stream().map(Tag::getName).collect(Collectors.joining("、"));
            lines.add("特征标签：" + tagNames);
        }

        //3) 进行中的护理计划与等级
        CarePlan activePlan = carePlanService.getOne(new LambdaQueryWrapper<CarePlan>()
                .eq(CarePlan::getElderId, elderId).eq(CarePlan::getStatus, 1)
                .orderByDesc(CarePlan::getCreateTime).last("LIMIT 1"));
        if (activePlan != null) {
            String levelName = activePlan.getCareLevelId() == null ? "" :
                    Optional.ofNullable(careLevelService.getById(activePlan.getCareLevelId())).map(CareLevel::getName).orElse("");
            lines.add("当前护理计划：" + safe(activePlan.getName())
                    + (levelName.isEmpty() ? "" : "（等级：" + levelName + "）")
                    + "，起止：" + fmtDate(activePlan.getStartDate()) + " ~ " + fmtDate(activePlan.getEndDate()));
        }

        //4) 最近护理任务执行记录（重点看执行结果/异常描述）
        List<CareTask> tasks = careTaskService.list(new LambdaQueryWrapper<CareTask>()
                .eq(CareTask::getElderId, elderId)
                .orderByDesc(CareTask::getPlanExecuteDate).last("LIMIT 8"));
        List<String> taskLines = new ArrayList<>();
        for (CareTask t : tasks) {
            if (ObjectUtils.isEmpty(t.getExecuteResult()) && t.getStatus() != 1) {
                continue;
            }
            String status = t.getStatus() == null ? "未知" : taskStatusText(t.getStatus());
            taskLines.add(fmtDate(t.getPlanExecuteDate()) + " " + (t.getPlanExecuteTime() == null ? "" : t.getPlanExecuteTime())
                    + " " + safe(t.getCareItemName()) + "[" + status + "]"
                    + (ObjectUtils.isEmpty(t.getExecuteResult()) ? "" : " 结果：" + t.getExecuteResult())
                    + (ObjectUtils.isEmpty(t.getRemark()) ? "" : " 备注：" + t.getRemark()));
        }
        if (!taskLines.isEmpty()) {
            lines.add("最近护理记录：" + String.join("；", taskLines));
        }

        //5) 最近已完成体检及异常项
        List<ExamAppointment> appointments = examAppointmentService.list(new LambdaQueryWrapper<ExamAppointment>()
                .eq(ExamAppointment::getElderId, elderId).eq(ExamAppointment::getStatus, 2)
                .orderByDesc(ExamAppointment::getAppointmentDate).last("LIMIT 2"));
        if (!appointments.isEmpty()) {
            List<Long> appointmentIds = appointments.stream().map(ExamAppointment::getId).toList();
            List<ExamAppointmentItem> items = examAppointmentItemService.list(new LambdaQueryWrapper<ExamAppointmentItem>()
                    .in(ExamAppointmentItem::getAppointmentId, appointmentIds)
                    .and(w -> w.eq(ExamAppointmentItem::getAbnormal, 1).or().eq(ExamAppointmentItem::getStatus, 2)));
            List<String> itemLines = new ArrayList<>();
            for (ExamAppointmentItem item : items) {
                itemLines.add(safe(item.getItemName())
                        + (item.getResultValue() != null ? " " + item.getResultValue() : "")
                        + (ObjectUtils.isEmpty(item.getResultUnit()) ? "" : " " + item.getResultUnit())
                        + (ObjectUtils.isEmpty(item.getResultText()) ? "" : " " + item.getResultText())
                        + (item.getAbnormal() != null && item.getAbnormal() == 1 ? "[异常]" : "")
                        + (ObjectUtils.isEmpty(item.getRemark()) ? "" : " " + item.getRemark()));
            }
            if (!itemLines.isEmpty()) {
                lines.add("最近体检异常项/结果：" + String.join("；", itemLines));
            }
        }

        String context = String.join("\n", lines);
        return context.length() > 2500 ? context.substring(0, 2500) : context;
    }

    /** 过滤与裁剪历史消息（角色白名单 + 长度限制），只取最近 MAX_HISTORY 条 */
    private List<AiChatMessage> sanitize(List<AiChatMessage> messages) {
        List<AiChatMessage> result = new ArrayList<>();
        int total = 0;
        for (int i = messages.size() - 1; i >= 0 && result.size() < MAX_HISTORY; i--) {
            AiChatMessage m = messages.get(i);
            if (m == null || m.content() == null) {
                continue;
            }
            String role = m.role();
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            String content = m.content().trim();
            if (content.isEmpty()) {
                continue;
            }
            if (content.length() > MAX_MSG_CHARS) {
                content = content.substring(0, MAX_MSG_CHARS);
            }
            total += content.length();
            if (total > MAX_TOTAL_CHARS) {
                break;
            }
            result.add(new AiChatMessage(role, content));
        }
        Collections.reverse(result);
        return result;
    }

    private String callDashScope(String systemText, List<AiChatMessage> history) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, String>> payloadMessages = new ArrayList<>();
        payloadMessages.add(Map.of("role", "system", "content", systemText));
        history.forEach(m -> payloadMessages.add(Map.of("role", m.role(), "content", m.content())));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", payloadMessages);
        body.put("temperature", 0.3);
        body.put("stream", false);

        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 100 != 2) {
            throw new ServiceException("AI 服务暂时不可用，请稍后再试");
        }
        JsonNode root = mapper.readTree(response.body());
        String content = root.path("choices").path(0).path("message").path("content").asText(null);
        if (content == null || content.isBlank()) {
            throw new ServiceException("AI 没有返回内容，请换个问法再试");
        }
        return content.trim();
    }

    private String fmtDate(java.util.Date date) {
        if (date == null) {
            return "未知";
        }
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(DATE_FMT);
    }

    private String fmtTime(java.util.Date time) {
        if (time == null) {
            return "";
        }
        return time.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime().toString();
    }

    private String elderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        String[] names = {"禁用", "启用", "请假", "退住中", "入住中", "已退住"};
        return status >= 0 && status < names.length ? names[status] : "未知";
    }

    private String taskStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        String[] names = {"待执行", "已完成", "已跳过"};
        return status >= 0 && status < names.length ? names[status] : "未知";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
