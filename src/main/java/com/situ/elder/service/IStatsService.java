package com.situ.elder.service;

import java.util.Map;

/**
 * 统计服务
 */
public interface IStatsService {

    /**
     * 首页看板统计概览：
     * elderStatus / elderAge / careLevelPlan / taskTrend
     */
    Map<String, Object> getOverview();

    /**
     * 数据大屏指标（含 overview 全部 + todayTask/appointmentNext7/abnormalRecent7/visitToday）
     */
    Map<String, Object> getScreen();
}
