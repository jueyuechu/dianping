package com.dianping.common;

import com.dianping.common.utils.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void encodeShouldProduceBcryptHash() {
        String hash = PasswordUtil.encode("123456");
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"), "应为 BCrypt 密文");
        assertTrue(PasswordUtil.isEncoded(hash));
        assertTrue(PasswordUtil.matches("123456", hash));
        assertFalse(PasswordUtil.matches("wrong", hash));
    }

    @Test
    void matchesShouldSupportLegacyPlaintext() {
        assertTrue(PasswordUtil.matches("123456", "123456"));
        assertFalse(PasswordUtil.matches("123456", "654321"));
        assertFalse(PasswordUtil.isEncoded("123456"));
    }

    @Test
    void nullPasswordShouldNotMatch() {
        assertFalse(PasswordUtil.matches(null, "123456"));
        assertFalse(PasswordUtil.matches("123456", null));
    }
}
