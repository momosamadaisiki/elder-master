package com.situ.elder.service;

import com.situ.elder.pojo.dto.AppAppointmentDTO;
import com.situ.elder.pojo.dto.ExamResultSaveRequest;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.query.ExamAppointmentAdminQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.vo.ExamAppointmentVO;

import java.util.List;

/**
 * <p>
 * 体检预约表 服务类
 * </p>
 *
 */
public interface IExamAppointmentService extends IService<ExamAppointment> {

    /**
     * 提交体检预约（同时写入套餐内项目的快照）
     */
    void add(AppAppointmentDTO appAppointmentDTO, Long elderId);

    /**
     * 查询老人的预约列表（含套餐名、体检人姓名、项目数）
     */
    List<ExamAppointmentVO> listByElderId(Long elderId);

    /**
     * 取消预约（只能取消自己的、待体检状态的预约）
     */
    void cancel(Long id, Long elderId);

    // ============ 后台：体检登记/完成 ============

    /** 后台分页（含老人名/套餐名/项目进度） */
    com.baomidou.mybatisplus.core.metadata.IPage<ExamAppointmentVO> adminPage(ExamAppointmentAdminQuery query);

    /** 后台详情：预约 + 明细（明细带项目类型与参考范围，用于自动判定） */
    java.util.Map<String, Object> getAdminDetail(Long id);

    /** 老人端本人查看体检报告 */
    java.util.Map<String, Object> report(Long id, Long elderId);

    /** 保存结果并完成体检（自动判定，可手动覆盖 abnormal） */
    void saveResults(Long id, ExamResultSaveRequest request);

    /** 后台取消（仅待体检） */
    void adminCancel(Long id);
}
