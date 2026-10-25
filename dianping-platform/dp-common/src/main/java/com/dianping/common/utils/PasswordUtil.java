package com.dianping.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具：BCrypt 哈希与校验
 * 兼容历史明文密码：校验通过后由登录逻辑自动升级为 BCrypt 存储
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 生成 BCrypt 哈希 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验密码：支持 BCrypt 与历史明文 */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isEncoded(storedPassword)) {
            return ENCODER.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    /** 判断是否已是 BCrypt 密文 */
    public static boolean isEncoded(String password) {
        return password != null
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
