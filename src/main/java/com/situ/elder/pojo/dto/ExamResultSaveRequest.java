package com.situ.elder.pojo.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 体检结果录入请求
 */
public record ExamResultSaveRequest(List<Row> items) {

    /**
     * 单个明细结果：abnormal 为空时后端按参考范围自动判定
     */
    public static record Row(Long id, BigDecimal resultValue, String resultText,
                             Integer abnormal, String remark) {
    }
}
