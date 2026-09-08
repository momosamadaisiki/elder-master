package com.situ.elder.service;

import com.situ.elder.pojo.dto.AiChatMessage;

import java.util.List;

/**
 * 康养小智 AI 助手服务
 */
public interface IAiChatService {

    /**
     * 携带老人健康上下文发起一轮对话
     *
     * @param elderId  当前登录老人 id（token 解析所得）
     * @param messages 前端传来的历史消息（user/assistant，不含 system）
     * @return AI 回复文本
     */
    String chat(Long elderId, List<AiChatMessage> messages);
}
