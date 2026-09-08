package com.situ.elder.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具：BCrypt 加密（不再明文存储）
 * 兼容旧数据：登录时若库中仍是明文且与输入一致，自动升级为 BCrypt。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** BCrypt 加密 */
    public static String encode(String raw) {
        if (raw == null) {
            raw = "";
        }
        return ENCODER.encode(raw);
    }

    /** 校验明文与存储密文是否匹配 */
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        return ENCODER.matches(raw, stored);
    }

    /** 是否已是 BCrypt 密文（$2a/$2b/$2y 开头） */
    public static boolean isBcrypt(String stored) {
        return stored != null && stored.startsWith("$2");
    }
}
