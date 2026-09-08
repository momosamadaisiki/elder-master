package com.situ.elder.pojo.dto;

import java.math.BigDecimal;

/**
 * 新增收费项目请求
 */
public record ChargeAddRequest(Long elderId, String itemName, BigDecimal amount,
                               Integer chargeType, String chargeMonth, String remark) {
}
