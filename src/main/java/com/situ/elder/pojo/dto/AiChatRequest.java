package com.situ.elder.pojo.dto;

import java.util.List;

/**
 * AI 对话请求：由前端携带历史消息（不含 system）
 */
public record AiChatRequest(List<AiChatMessage> messages) {
}
