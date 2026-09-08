package com.situ.elder.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.dto.ExamResultSaveRequest;
import com.situ.elder.pojo.query.ExamAppointmentAdminQuery;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台：体检登记/完成（把“待体检”的预约录完结果置为已完成）
 */
@RestController
@RequestMapping("/admin/exam-appointments")
public class ExamAppointmentAdminController {

    @Autowired
    private IExamAppointmentService examAppointmentService;

    @GetMapping
    public Result<IPage<ExamAppointmentVO>> list(ExamAppointmentAdminQuery query) {
        return Result.ok(examAppointmentService.adminPage(query));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(examAppointmentService.getAdminDetail(id));
    }

    /** 保存结果并完成体检 */
    @PutMapping("/{id}/items")
    public Result saveResults(@PathVariable Long id, @RequestBody ExamResultSaveRequest request) {
        examAppointmentService.saveResults(id, request);
        return Result.ok("体检结果已保存，该预约已完成");
    }

    /** 后台取消（仅待体检） */
    @PutMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id) {
        examAppointmentService.adminCancel(id);
        return Result.ok("已取消");
    }
}
