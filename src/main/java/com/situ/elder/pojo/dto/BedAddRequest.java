package com.situ.elder.pojo.dto;

import java.util.List;

/**
 * 批量新增床位请求
 */
public record BedAddRequest(Long roomId, List<String> bedNos) {
}
