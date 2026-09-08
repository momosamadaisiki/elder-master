package com.situ.elder.config;

import com.situ.elder.interceptor.LoginInterceptor;
import com.situ.elder.interceptor.RoleAccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RoleAccessInterceptor roleAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1) 登录校验（拦截所有请求，放行登录接口）
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/users/login", "/app/elders/login", "/app/family/login");
        // 2) 角色级接口访问控制（护工只能访问自己的护理任务）
        registry.addInterceptor(roleAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/users/login", "/app/elders/login", "/app/family/login");
    }

    //把前台String改成Date类型
    @Override
    public void addFormatters(FormatterRegistry registry) {
        String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        List<String> patterns = Arrays.asList(
                "yyyy-MM-dd HH:mm:ss",       // 普通格式
                "yyyy-MM-dd",                // 普通格式
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",// ISO 格式：2025-08-25T09:21:40.922Z
                "yyyy-MM-dd'T'HH:mm:ssX"     // ISO 格式：2025-08-25T09:21:40Z
        );

        registry.addConverter(new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                for (String pattern : patterns) {
                    try {
                        return new SimpleDateFormat(pattern).parse(source);
                    } catch (ParseException ignored) {
                    }
                }
                throw new RuntimeException("时间转换失败：" + source);
            }
        });
    }
}
