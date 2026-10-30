package com.dianping.common;

import com.dianping.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "dianping-test-jwt-secret-key-0123456789abcdef";

    @Test
    void createAndParseShouldRoundTripClaims() {
        String token = JwtUtil.createJWT(SECRET, 60_000L, Map.of("userId", 100L));
        Claims claims = JwtUtil.parseJWT(SECRET, token);
        assertEquals(100L, claims.get("userId", Long.class));
    }

    @Test
    void parseShouldRejectWrongSecret() {
        String token = JwtUtil.createJWT(SECRET, 60_000L, Map.of("userId", 1L));
        assertThrows(Exception.class, () -> JwtUtil.parseJWT(SECRET + "-wrong", token));
    }

    @Test
    void resolveTokenShouldHandleBearerPrefix() {
        assertEquals("abc", JwtUtil.resolveToken("Bearer abc"));
        assertEquals("abc", JwtUtil.resolveToken("abc"));
        assertNull(JwtUtil.resolveToken(null));
        assertNull(JwtUtil.resolveToken("   "));
    }
}
