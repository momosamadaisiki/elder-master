package com.situ.elder.controller.app;

import com.situ.elder.pojo.dto.AppAppointmentDTO;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 老人手机端体检预约控制器
 */
@RestController
@RequestMapping("/app/appointment")
public class AppAppointmentController {

    @Autowired
    private IExamAppointmentService examAppointmentService;

    /**
     * 提交体检预约
     * POST /app/appointment
     */
    @PostMapping
    public Result add(@RequestHeader("Authorization") String token,
                      @RequestBody AppAppointmentDTO appAppointmentDTO) {
        Long elderId = getElderIdFromToken(token);
        examAppointmentService.add(appAppointmentDTO, elderId);
        return Result.ok("预约成功");
    }

    /**
     * 我的预约列表
     * GET /app/appointment
     */
    @GetMapping
    public Result<List<ExamAppointmentVO>> list(@RequestHeader("Authorization") String token) {
        Long elderId = getElderIdFromToken(token);
        return Result.ok(examAppointmentService.listByElderId(elderId));
    }

    /**
     * 取消预约
     * PUT /app/appointment/1/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result cancel(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Long elderId = getElderIdFromToken(token);
        examAppointmentService.cancel(id, elderId);
        return Result.ok("取消成功");
    }

    /**
     * 查看体检报告（仅本人，需已完成）
     * GET /app/appointment/1/report
     */
    @GetMapping("/{id}/report")
    public Result<Map<String, Object>> report(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Long elderId = getElderIdFromToken(token);
        return Result.ok(examAppointmentService.report(id, elderId));
    }

    /**
     * 从token中解析当前登录老人的id
     */
    private Long getElderIdFromToken(String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        if ("family".equals(map.get("type"))) {
            throw new com.situ.elder.exception.ServiceException("请使用老人账号操作");
        }
        Integer id = (Integer) map.get("id");
        return id.longValue();
    }
}
