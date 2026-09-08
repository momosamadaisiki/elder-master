package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.dto.AppAppointmentDTO;
import com.situ.elder.pojo.dto.ExamResultSaveRequest;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.entity.ExamAppointmentItem;
import com.situ.elder.pojo.entity.ExamItem;
import com.situ.elder.pojo.entity.ExamPackage;
import com.situ.elder.pojo.entity.ExamPackageItem;
import com.situ.elder.mapper.ExamAppointmentMapper;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.pojo.query.ExamAppointmentAdminQuery;
import com.situ.elder.service.IElderService;
import com.situ.elder.service.IExamAppointmentItemService;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.service.IExamItemService;
import com.situ.elder.service.IExamPackageItemService;
import com.situ.elder.service.IExamPackageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 体检预约表 服务实现类
 * </p>
 */
@Service
public class ExamAppointmentServiceImpl extends ServiceImpl<ExamAppointmentMapper, ExamAppointment> implements IExamAppointmentService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private IElderService elderService;
    @Autowired
    private IExamPackageService examPackageService;
    @Autowired
    private IExamPackageItemService examPackageItemService;
    @Autowired
    private IExamItemService examItemService;
    @Autowired
    private IExamAppointmentItemService examAppointmentItemService;

    @Transactional
    @Override
    public void add(AppAppointmentDTO appAppointmentDTO, Long elderId) {
        //解析并校验日期、时间
        LocalDate appointmentDate;
        LocalTime appointmentTime;
        try {
            appointmentDate = LocalDate.parse(appAppointmentDTO.getDate());
            appointmentTime = LocalTime.parse(appAppointmentDTO.getTime());
        } catch (Exception e) {
            throw new ServiceException("预约日期或时间格式不正确");
        }
        if (LocalDateTime.of(appointmentDate, appointmentTime).isBefore(LocalDateTime.now())) {
            throw new ServiceException("预约时间必须晚于当前时间");
        }

        //校验套餐存在且上架
        ExamPackage examPackage = examPackageService.getById(appAppointmentDTO.getPackageId());
        if (examPackage == null) {
            throw new ServiceException("体检套餐不存在");
        }
        if (examPackage.getStatus() != 1) {
            throw new ServiceException("体检套餐已下架，无法预约");
        }

        //同一老人同一时段不能重复预约
        Long count = lambdaQuery()
                .eq(ExamAppointment::getElderId, elderId)
                .eq(ExamAppointment::getAppointmentDate, appointmentDate)
                .eq(ExamAppointment::getAppointmentTime, appointmentTime)
                .in(ExamAppointment::getStatus, 0, 1)
                .count();
        if (count > 0) {
            throw new ServiceException("该时间段您已有预约，请选择其他时间");
        }

        //保存预约（价格取套餐当前价格快照）
        ExamAppointment examAppointment = new ExamAppointment();
        examAppointment.setElderId(elderId);
        examAppointment.setPackageId(examPackage.getId());
        examAppointment.setAppointmentDate(appointmentDate);
        examAppointment.setAppointmentTime(appointmentTime);
        examAppointment.setPrice(examPackage.getPrice());
        examAppointment.setStatus(0);
        save(examAppointment);

        //写入套餐内项目的快照，后续体检结果直接录到这些明细上
        List<ExamPackageItem> examPackageItemList = examPackageItemService.lambdaQuery()
                .eq(ExamPackageItem::getPackageId, examPackage.getId())
                .orderByAsc(ExamPackageItem::getSort)
                .list();
        if (ObjectUtils.isEmpty(examPackageItemList)) {
            return;
        }
        List<Long> examItemIdList = examPackageItemList.stream().map(ExamPackageItem::getExamItemId).toList();
        Map<Long, ExamItem> examItemMap = examItemService.listByIds(examItemIdList).stream()
                .collect(Collectors.toMap(ExamItem::getId, Function.identity()));
        List<ExamAppointmentItem> examAppointmentItemList = new ArrayList<>();
        for (ExamPackageItem examPackageItem : examPackageItemList) {
            ExamItem examItem = examItemMap.get(examPackageItem.getExamItemId());
            if (examItem == null) {
                continue;
            }
            ExamAppointmentItem examAppointmentItem = new ExamAppointmentItem();
            examAppointmentItem.setAppointmentId(examAppointment.getId());
            examAppointmentItem.setExamItemId(examItem.getId());
            examAppointmentItem.setItemName(examItem.getName());
            examAppointmentItem.setStatus(0);
            examAppointmentItem.setAbnormal(0);
            examAppointmentItemList.add(examAppointmentItem);
        }
        examAppointmentItemService.saveBatch(examAppointmentItemList);
    }

    @Override
    public List<ExamAppointmentVO> listByElderId(Long elderId) {
        List<ExamAppointment> examAppointmentList = lambdaQuery()
                .eq(ExamAppointment::getElderId, elderId)
                .orderByDesc(ExamAppointment::getAppointmentDate)
                .orderByDesc(ExamAppointment::getAppointmentTime)
                .list();
        if (ObjectUtils.isEmpty(examAppointmentList)) {
            return List.of();
        }

        //体检人姓名
        Elder elder = elderService.getById(elderId);
        String elderName = elder != null ? elder.getName() : "";

        //批量查套餐名
        List<Long> packageIdList = examAppointmentList.stream()
                .map(ExamAppointment::getPackageId).distinct().toList();
        Map<Long, String> packageNameMap = examPackageService.listByIds(packageIdList).stream()
                .collect(Collectors.toMap(ExamPackage::getId, ExamPackage::getName));

        //批量统计每个套餐的项目数量
        Map<Long, Long> itemCountMap = examPackageItemService.lambdaQuery()
                .in(ExamPackageItem::getPackageId, packageIdList)
                .list()
                .stream()
                .collect(Collectors.groupingBy(ExamPackageItem::getPackageId, Collectors.counting()));

        return examAppointmentList.stream().map(examAppointment -> {
            ExamAppointmentVO examAppointmentVO = new ExamAppointmentVO();
            examAppointmentVO.setId(examAppointment.getId());
            examAppointmentVO.setPackageId(examAppointment.getPackageId());
            examAppointmentVO.setPackageName(packageNameMap.getOrDefault(examAppointment.getPackageId(), "已删除套餐"));
            examAppointmentVO.setElderName(elderName);
            examAppointmentVO.setAppointmentDate(examAppointment.getAppointmentDate().toString());
            examAppointmentVO.setAppointmentTime(examAppointment.getAppointmentTime().format(TIME_FORMATTER));
            examAppointmentVO.setPrice(examAppointment.getPrice());
            examAppointmentVO.setStatus(examAppointment.getStatus());
            examAppointmentVO.setExamItemCount(itemCountMap.getOrDefault(examAppointment.getPackageId(), 0L).intValue());
            return examAppointmentVO;
        }).toList();
    }

    @Override
    public void cancel(Long id, Long elderId) {
        ExamAppointment examAppointment = getById(id);
        if (examAppointment == null) {
            throw new ServiceException("预约不存在");
        }
        //只能取消自己的预约
        if (!examAppointment.getElderId().equals(elderId)) {
            throw new ServiceException("无权取消他人的预约");
        }
        //只有待体检的预约才能取消
        if (examAppointment.getStatus() != 0) {
            throw new ServiceException("当前状态不允许取消");
        }

        ExamAppointment update = new ExamAppointment();
        update.setId(id);
        update.setStatus(3);
        updateById(update);
    }

    // ==================== 后台：体检登记/完成 ====================

    @Override
    public IPage<ExamAppointmentVO> adminPage(ExamAppointmentAdminQuery query) {
        IPage<ExamAppointment> page = new Page<>(query.getPage(), query.getLimit());
        LambdaQueryWrapper<ExamAppointment> wrapper = new LambdaQueryWrapper<>();
        if (!ObjectUtils.isEmpty(query.getElderName())) {
            List<Long> elderIds = elderService.list(new LambdaQueryWrapper<Elder>()
                            .like(Elder::getName, query.getElderName())).stream()
                    .map(Elder::getId).toList();
            if (elderIds.isEmpty()) {
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
            wrapper.in(ExamAppointment::getElderId, elderIds);
        }
        wrapper.eq(!ObjectUtils.isEmpty(query.getStatus()), ExamAppointment::getStatus, query.getStatus())
                .ge(!ObjectUtils.isEmpty(query.getBeginDate()), ExamAppointment::getAppointmentDate, query.getBeginDate())
                .le(!ObjectUtils.isEmpty(query.getEndDate()), ExamAppointment::getAppointmentDate, query.getEndDate())
                .orderByDesc(ExamAppointment::getAppointmentDate)
                .orderByDesc(ExamAppointment::getAppointmentTime);

        IPage<ExamAppointment> result = page(page, wrapper);
        List<ExamAppointment> records = result.getRecords();
        List<ExamAppointmentVO> voList = new ArrayList<>();
        if (!records.isEmpty()) {
            //老人名/套餐名
            List<Long> elderIds = records.stream().map(ExamAppointment::getElderId).distinct().toList();
            List<Long> packageIds = records.stream().map(ExamAppointment::getPackageId).distinct().toList();
            Map<Long, String> elderNameMap = elderService.listByIds(elderIds).stream()
                    .collect(Collectors.toMap(Elder::getId, Elder::getName));
            Map<Long, String> packageNameMap = examPackageService.listByIds(packageIds).stream()
                    .collect(Collectors.toMap(ExamPackage::getId, ExamPackage::getName));

            //明细进度：总项目数/已出结果/异常
            List<Long> appointmentIds = records.stream().map(ExamAppointment::getId).toList();
            Map<Long, List<ExamAppointmentItem>> itemMap = examAppointmentItemService
                    .list(new LambdaQueryWrapper<ExamAppointmentItem>().in(ExamAppointmentItem::getAppointmentId, appointmentIds))
                    .stream().collect(Collectors.groupingBy(ExamAppointmentItem::getAppointmentId));

            for (ExamAppointment a : records) {
                List<ExamAppointmentItem> items = itemMap.getOrDefault(a.getId(), List.of());
                ExamAppointmentVO vo = new ExamAppointmentVO();
                vo.setId(a.getId());
                vo.setPackageId(a.getPackageId());
                vo.setPackageName(packageNameMap.getOrDefault(a.getPackageId(), "已删除套餐"));
                vo.setElderName(elderNameMap.get(a.getElderId()));
                vo.setAppointmentDate(a.getAppointmentDate().toString());
                vo.setAppointmentTime(a.getAppointmentTime().format(TIME_FORMATTER));
                vo.setPrice(a.getPrice());
                vo.setStatus(a.getStatus());
                vo.setExamItemCount(items.size());
                vo.setDoneItemCount((int) items.stream().filter(i -> i.getStatus() != null && (i.getStatus() == 1 || i.getStatus() == 2)).count());
                vo.setAbnormalCount((int) items.stream().filter(i -> i.getAbnormal() != null && i.getAbnormal() == 1).count());
                voList.add(vo);
            }
        }
        Page<ExamAppointmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Map<String, Object> getAdminDetail(Long id) {
        return buildDetail(id);
    }

    /** 老人端本人查看报告 */
    @Override
    public Map<String, Object> report(Long id, Long elderId) {
        ExamAppointment appointment = getById(id);
        if (appointment == null) {
            throw new ServiceException("体检记录不存在");
        }
        if (!appointment.getElderId().equals(elderId)) {
            throw new ServiceException("无权查看他人的体检报告");
        }
        return buildDetail(id);
    }

    private Map<String, Object> buildDetail(Long id) {
        ExamAppointment appointment = getById(id);
        if (appointment == null) {
            throw new ServiceException("体检预约不存在");
        }
        ExamPackage examPackage = examPackageService.getById(appointment.getPackageId());
        Elder elder = elderService.getById(appointment.getElderId());

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", appointment.getId());
        result.put("elderName", elder == null ? "" : elder.getName());
        result.put("packageName", examPackage == null ? "已删除套餐" : examPackage.getName());
        result.put("appointmentDate", appointment.getAppointmentDate().toString());
        result.put("appointmentTime", appointment.getAppointmentTime().format(TIME_FORMATTER));
        result.put("price", appointment.getPrice());
        result.put("status", appointment.getStatus());

        List<ExamAppointmentItem> items = examAppointmentItemService.list(
                new LambdaQueryWrapper<ExamAppointmentItem>()
                        .eq(ExamAppointmentItem::getAppointmentId, id)
                        .orderByAsc(ExamAppointmentItem::getId));
        //项目元信息（类型/参考范围，用于自动判定）
        Map<Long, ExamItem> metaMap = items.isEmpty() ? Map.of()
                : examItemService.listByIds(items.stream().map(ExamAppointmentItem::getExamItemId).distinct().toList())
                .stream().collect(Collectors.toMap(ExamItem::getId, i -> i));

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (ExamAppointmentItem it : items) {
            ExamItem meta = metaMap.get(it.getExamItemId());
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", it.getId());
            m.put("examItemId", it.getExamItemId());
            m.put("itemName", it.getItemName());
            m.put("resultValue", it.getResultValue());
            m.put("resultUnit", it.getResultUnit());
            m.put("resultText", it.getResultText());
            m.put("status", it.getStatus());
            m.put("abnormal", it.getAbnormal());
            m.put("remark", it.getRemark());
            m.put("resultType", meta == null ? 0 : meta.getResultType());
            m.put("referenceMin", meta == null ? null : meta.getReferenceMin());
            m.put("referenceMax", meta == null ? null : meta.getReferenceMax());
            m.put("referenceUnit", meta == null ? null : meta.getReferenceUnit());
            itemList.add(m);
        }
        result.put("items", itemList);
        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void saveResults(Long id, ExamResultSaveRequest request) {
        ExamAppointment appointment = getById(id);
        if (appointment == null) {
            throw new ServiceException("体检预约不存在");
        }
        if (appointment.getStatus() != 0 && appointment.getStatus() != 1) {
            throw new ServiceException("当前状态不能录入体检结果");
        }
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new ServiceException("请至少录入一个体检项目结果");
        }

        List<ExamResultSaveRequest.Row> rows = request.items();
        List<Long> rowIds = rows.stream().map(ExamResultSaveRequest.Row::id).toList();
        Map<Long, ExamAppointmentItem> itemMap = examAppointmentItemService.listByIds(rowIds).stream()
                .collect(Collectors.toMap(ExamAppointmentItem::getId, i -> i));
        List<Long> metaIds = itemMap.values().stream().map(ExamAppointmentItem::getExamItemId).distinct().toList();
        Map<Long, ExamItem> metaMap = metaIds.isEmpty() ? Map.of()
                : examItemService.listByIds(metaIds).stream().collect(Collectors.toMap(ExamItem::getId, i -> i));

        for (ExamResultSaveRequest.Row row : rows) {
            ExamAppointmentItem item = itemMap.get(row.id());
            if (item == null || !item.getAppointmentId().equals(id)) {
                throw new ServiceException("存在不属于该体检单的项目，请刷新后重试");
            }
            ExamItem meta = metaMap.get(item.getExamItemId());

            item.setResultValue(row.resultValue());
            item.setResultText(row.resultText());
            item.setRemark(row.remark());
            if (row.resultValue() != null && meta != null) {
                item.setResultUnit(meta.getReferenceUnit());
            }

            int abnormal = row.abnormal() != null ? row.abnormal() : autoJudge(item, meta);
            item.setAbnormal(abnormal);
            //状态：没填值=未完成；有值按异常与否
            boolean hasValue = row.resultValue() != null || (row.resultText() != null && !row.resultText().isEmpty());
            item.setStatus(hasValue ? (abnormal == 1 ? 2 : 1) : 3);
            examAppointmentItemService.updateById(item);
        }

        ExamAppointment update = new ExamAppointment();
        update.setId(id);
        update.setStatus(2); //已完成
        updateById(update);
    }

    /** 数值型按参考范围自动判定；无参考范围或文本型默认正常 */
    private int autoJudge(ExamAppointmentItem item, ExamItem meta) {
        if (item.getResultValue() == null || meta == null || meta.getResultType() == null || meta.getResultType() != 1) {
            return 0;
        }
        if (meta.getReferenceMin() != null && item.getResultValue().compareTo(meta.getReferenceMin()) < 0) {
            return 1;
        }
        if (meta.getReferenceMax() != null && item.getResultValue().compareTo(meta.getReferenceMax()) > 0) {
            return 1;
        }
        return 0;
    }

    @Override
    public void adminCancel(Long id) {
        ExamAppointment appointment = getById(id);
        if (appointment == null) {
            throw new ServiceException("体检预约不存在");
        }
        if (appointment.getStatus() != 0) {
            throw new ServiceException("仅待体检的预约可取消");
        }
        ExamAppointment update = new ExamAppointment();
        update.setId(id);
        update.setStatus(3);
        updateById(update);
    }
}
