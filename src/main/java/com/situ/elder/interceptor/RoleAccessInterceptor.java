package com.situ.elder.interceptor;

import com.situ.elder.service.IUserService;
import com.situ.elder.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

/**
 * 角色级访问控制：护工(hugong)在后台仅允许访问自己的护理任务
 * - GET /admin/care-task/**（列表/详情，Controller 内再按 userId 过滤）
 * - PUT /admin/care-task/{id}（回填自己的任务打卡，Controller 内校验归属）
 * - GET /admin/users/userInfo、PUT /admin/users/resetPassword（个人基础操作）
 * 其余 /admin/** 一律 403。管理员(admin)不受限。
 */
@Component
public class RoleAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/admin/")) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(token)) {
            return true; //未带 token 交给 LoginInterceptor 处理 401
        }
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        if (id == null) {
            return true;
        }
        List<String> roleCodes = userService.listRoleCodes(id.longValue());
        if (!roleCodes.contains("hugong")) {
            return true;
        }

        String method = request.getMethod();
        boolean allowed =
                ("GET".equals(method) && (uri.startsWith("/admin/care-task") || uri.equals("/admin/users/userInfo")))
                        || ("PUT".equals(method) && (uri.startsWith("/admin/care-task/") || uri.equals("/admin/users/resetPassword")));
        if (allowed) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":0,\"msg\":\"护工账号仅可查看并回填分配给自己的护理任务\"}");
        return false;
    }
}
