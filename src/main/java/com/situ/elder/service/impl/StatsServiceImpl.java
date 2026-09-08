package com.situ.elder.service.impl;

import com.situ.elder.mapper.StatsMapper;
import com.situ.elder.service.IStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计聚合服务：全部基于现有表 count/group by，无新增表
 */
@Service
public class StatsServiceImpl implements IStatsService {

    /** 老人状态文案（与 el-dashboard 中 elder.status 注释一致） */
    private static final String[] ELDER_STATUS_NAMES = {"禁用", "启用", "请假", "退住中", "入住中", "已退住"};
    /** 年龄分桶文案：0:<60 1:60-69 2:70-79 3:80-89 4:90+ 5:未知 */
    private static final String[] ELDER_AGE_NAMES = {"60岁以下", "60-69岁", "70-79岁", "80-89岁", "90岁以上", "未知"};

    @Autowired
    private StatsMapper statsMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        result.put("elderStatus", buildElderStatus());
        result.put("elderAge", buildElderAge());
        result.put("careLevelPlan", buildCareLevelPlan());
        result.put("taskTrend", buildTaskTrend());
        return result;
    }

    /** 老人状态分布：固定 0~5 顺序、缺省补 0，返回 [{key, name, value}] */
    private List<Map<String, Object>> buildElderStatus() {
        Map<Integer, Long> countMap = toCountMap(statsMapper.elderStatusCount(), "bucketKey", "bucketValue");
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < ELDER_STATUS_NAMES.length; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", i);
            item.put("name", ELDER_STATUS_NAMES[i]);
            item.put("value", countMap.getOrDefault(i, 0L));
            list.add(item);
        }
        return list;
    }

    /** 老人年龄分布：固定 6 桶、缺省补 0 */
    private List<Map<String, Object>> buildElderAge() {
        Map<Integer, Long> countMap = toCountMap(statsMapper.elderAgeCount(), "bucketKey", "bucketValue");
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < ELDER_AGE_NAMES.length; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", i);
            item.put("name", ELDER_AGE_NAMES[i]);
            item.put("value", countMap.getOrDefault(i, 0L));
            list.add(item);
        }
        return list;
    }

    /** 护理计划等级分布：name -> 计划数 */
    private List<Map<String, Object>> buildCareLevelPlan() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : statsMapper.careLevelPlanCount()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", String.valueOf(row.get("levelName")));
            item.put("value", numberValue(row.get("levelValue")));
            list.add(item);
        }
        return list;
    }

    /** 护理任务近 7 天完成趋势 */
    private Map<String, Object> buildTaskTrend() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        Map<LocalDate, long[]> rowMap = new HashMap<>();
        for (Map<String, Object> row : statsMapper.taskTrend(start.toString(), today.toString())) {
            LocalDate date = LocalDate.parse(String.valueOf(row.get("taskDate")).substring(0, 10));
            long total = numberValue(row.get("taskTotal"));
            long done = numberValue(row.get("taskDone"));
            rowMap.put(date, new long[]{total, done});
        }

        List<String> dates = new ArrayList<>();
        List<Long> totals = new ArrayList<>();
        List<Long> dones = new ArrayList<>();
        List<BigDecimal> rates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            long[] v = rowMap.getOrDefault(d, new long[]{0L, 0L});
            dates.add(d.toString());
            totals.add(v[0]);
            dones.add(v[1]);
            BigDecimal rate = v[0] == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(v[1] * 100.0 / v[0]).setScale(1, RoundingMode.HALF_UP);
            rates.add(rate);
        }

        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("dates", dates);
        trend.put("total", totals);
        trend.put("done", dones);
        trend.put("rate", rates);
        return trend;
    }

    @Override
    public Map<String, Object> getScreen() {
        Map<String, Object> result = new LinkedHashMap<>();
        //复用首页分布/趋势
        result.put("elderStatus", buildElderStatus());
        result.put("elderAge", buildElderAge());
        result.put("careLevelPlan", buildCareLevelPlan());
        result.put("taskTrend", buildTaskTrend());

        //大屏实时指标
        long todayTotal = statsMapper.todayTaskTotal();
        long todayDone = statsMapper.todayTaskDone();
        Map<String, Object> todayTask = new LinkedHashMap<>();
        todayTask.put("total", todayTotal);
        todayTask.put("done", todayDone);
        todayTask.put("rate", todayTotal == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(todayDone * 100.0 / todayTotal).setScale(1, RoundingMode.HALF_UP));

        result.put("todayTask", todayTask);
        result.put("appointmentNext7", statsMapper.appointmentNext7());
        result.put("abnormalRecent7", statsMapper.abnormalRecent7());

        Map<String, Object> visitToday = new LinkedHashMap<>();
        visitToday.put("total", statsMapper.visitTodayTotal());
        visitToday.put("arrived", statsMapper.visitTodayArrived());
        result.put("visitToday", visitToday);
        return result;
    }

    private Map<Integer, Long> toCountMap(List<Map<String, Object>> rows, String keyCol, String valueCol) {
        Map<Integer, Long> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            map.put((int) numberValue(row.get(keyCol)), numberValue(row.get(valueCol)));
        }
        return map;
    }

    private long numberValue(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
