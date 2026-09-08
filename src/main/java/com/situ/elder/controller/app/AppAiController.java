package com.situ.elder.controller.app;

import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.dto.AiChatRequest;
import com.situ.elder.service.IAiChatService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 康养小智 AI 健康助手（老人/家属端）
 * POST /app/ai/chat
 */
@RestController
@RequestMapping("/app/ai")
public class AppAiController {

    @Autowired
    private IAiChatService aiChatService;

    @PostMapping("/chat")
    public Result chat(@RequestHeader("Authorization") String token, @RequestBody AiChatRequest request) {
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            throw new ServiceException("消息不能为空");
        }
        //token 由 LoginInterceptor 校验过，这里只取当前老人 id（与 /app/elders/* 一致）
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        if (id == null) {
            throw new ServiceException("登录已过期，请重新登录");
        }
        String reply = aiChatService.chat(id.longValue(), request.messages());
        return Result.ok(Map.of("reply", reply));
    }
}
