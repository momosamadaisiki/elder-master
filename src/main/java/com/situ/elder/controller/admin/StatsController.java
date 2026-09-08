package com.situ.elder.controller.admin;

import com.situ.elder.service.IStatsService;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计接口（首页看板数据统计可视化）
 */
@RestController
@RequestMapping("/admin/stats")
public class StatsController {

    @Autowired
    private IStatsService statsService;

    /**
     * 首页看板统计概览
     * GET /admin/stats/overview
     */
    @GetMapping("/overview")
    public Result overview() {
        return Result.ok(statsService.getOverview());
    }

    /**
     * 数据大屏指标
     * GET /admin/stats/screen
     */
    @GetMapping("/screen")
    public Result screen() {
        return Result.ok(statsService.getScreen());
    }
}
