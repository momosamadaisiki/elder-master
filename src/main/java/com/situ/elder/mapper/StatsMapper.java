package com.situ.elder.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计聚合查询（基于现有表，无需新增表）
 */
public interface StatsMapper {

    /**
     * 老人状态分布：elder.status -> 数量
     */
    @Select("SELECT status AS bucketKey, COUNT(*) AS bucketValue " +
            "FROM elder WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> elderStatusCount();

    /**
     * 老人年龄分布（按当前年龄分桶，0:<60 1:60-69 2:70-79 3:80-89 4:90+ 5:未知）
     */
    @Select("SELECT CASE " +
            " WHEN birthday IS NULL THEN 5 " +
            " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) < 60 THEN 0 " +
            " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) < 70 THEN 1 " +
            " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) < 80 THEN 2 " +
            " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) < 90 THEN 3 " +
            " ELSE 4 END AS bucketKey, COUNT(*) AS bucketValue " +
            "FROM elder WHERE deleted = 0 GROUP BY bucketKey")
    List<Map<String, Object>> elderAgeCount();

    /**
     * 护理计划-护理等级分布
     */
    @Select("SELECT IFNULL(cl.name, '未设置') AS levelName, COUNT(*) AS levelValue " +
            "FROM care_plan cp LEFT JOIN care_level cl ON cp.care_level_id = cl.id " +
            "GROUP BY cp.care_level_id, cl.name ORDER BY levelValue DESC")
    List<Map<String, Object>> careLevelPlanCount();

    /**
     * 护理任务每日总量与完成量（近 N 天）
     */
    @Select("SELECT plan_execute_date AS taskDate, COUNT(*) AS taskTotal, " +
            "COALESCE(SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END), 0) AS taskDone " +
            "FROM care_task " +
            "WHERE plan_execute_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY plan_execute_date ORDER BY plan_execute_date")
    List<Map<String, Object>> taskTrend(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // ==================== 大屏指标 ====================

    /** 今日护理任务总数 */
    @Select("SELECT COUNT(*) FROM care_task WHERE plan_execute_date = CURDATE()")
    Long todayTaskTotal();

    /** 今日已完成护理任务数 */
    @Select("SELECT COUNT(*) FROM care_task WHERE plan_execute_date = CURDATE() AND status = 1")
    Long todayTaskDone();

    /** 未来 7 天待体检预约数（待体检/体检中） */
    @Select("SELECT COUNT(*) FROM exam_appointment WHERE status IN (0,1) " +
            "AND appointment_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)")
    Long appointmentNext7();

    /** 近 7 天体检异常明细条数 */
    @Select("SELECT COUNT(DISTINCT i.id) FROM exam_appointment_item i " +
            "INNER JOIN exam_appointment a ON i.appointment_id = a.id " +
            "WHERE i.abnormal = 1 AND a.appointment_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND CURDATE()")
    Long abnormalRecent7();

    /** 今日探视/访客预约总数 */
    @Select("SELECT COUNT(*) FROM visit_record WHERE visit_date = CURDATE() AND deleted = 0")
    Long visitTodayTotal();

    /** 今日已到访人数 */
    @Select("SELECT COUNT(*) FROM visit_record WHERE visit_date = CURDATE() AND arrive_time IS NOT NULL AND deleted = 0")
    Long visitTodayArrived();
}
