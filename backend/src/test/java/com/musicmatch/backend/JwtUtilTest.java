package com.musicmatch.backend;

import com.musicmatch.backend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                jwtUtil,
                "secret",
                "test_secret_key_very_long_for_tests_123456789"
        );

        ReflectionTestUtils.setField(jwtUtil, "accessExpirationMs", 900000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", 604800000L);
    }

    @Test
    void accessTokenIsValidAndAccess() {
        String token = jwtUtil.generateAccessToken(1L, "usuarioTest");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.isAccessToken(token)).isTrue();
        assertThat(jwtUtil.isRefreshToken(token)).isFalse();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    void refreshTokenIsValidAndRefresh() {
        String token = jwtUtil.generateRefreshToken(1L);

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.isRefreshToken(token)).isTrue();
        assertThat(jwtUtil.isAccessToken(token)).isFalse();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    void invalidTokenIsRejected() {
        String invalidToken = "token.invalido.sin.formato.correcto";

        assertThat(jwtUtil.isTokenValid(invalidToken)).isFalse();
        assertThat(jwtUtil.isAccessToken(invalidToken)).isFalse();
        assertThat(jwtUtil.isRefreshToken(invalidToken)).isFalse();
    }
}