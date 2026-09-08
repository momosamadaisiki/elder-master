package com.situ.elder.pojo.dto;

import lombok.Data;

/**
 * 老人手机端提交体检预约DTO
 */
@Data
public class AppAppointmentDTO {

    /**
     * 体检套餐ID
     */
    private Long packageId;

    /**
     * 预约日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 预约时间（HH:mm）
     */
    private String time;

}
