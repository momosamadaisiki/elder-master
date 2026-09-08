package com.situ.elder.controller.app;

import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.entity.ElderFamily;
import com.situ.elder.service.IElderProfileService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 家属端（ui-app 家属登录，只读查看绑定老人的健康信息）
 * POST /app/family/login
 * GET  /app/family/me
 */
@RestController
@RequestMapping("/app/family")
public class AppFamilyController {

    @Autowired
    private IElderProfileService elderProfileService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody Map<String, String> body) {
        ElderFamily family = elderProfileService.loginFamily(
                body == null ? null : body.get("name"),
                body == null ? null : body.get("password"));
        Map<String, Object> map = new HashMap<>();
        map.put("id", family.getId());
        map.put("name", family.getName());
        map.put("type", "family");
        String token = JwtUtil.createToken(map);
        return Result.ok("登录成功", token);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(@RequestHeader("Authorization") String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        if (!"family".equals(map.get("type"))) {
            throw new ServiceException("请使用家属账号登录");
        }
        Integer id = (Integer) map.get("id");
        return Result.ok(elderProfileService.familyElderViewOfFamily(id.longValue()));
    }
}
