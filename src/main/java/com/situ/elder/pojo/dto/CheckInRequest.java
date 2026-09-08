package com.situ.elder.pojo.dto;

/**
 * 办理入住请求
 */
public record CheckInRequest(Long elderId, Long bedId, Long careLevelId, String checkInDate, String remark) {
}
