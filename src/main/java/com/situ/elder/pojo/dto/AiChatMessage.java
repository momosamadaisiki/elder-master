package com.situ.elder.pojo.dto;

/**
 * 单条对话消息（角色：system/user/assistant）
 */
public record AiChatMessage(String role, String content) {
}
