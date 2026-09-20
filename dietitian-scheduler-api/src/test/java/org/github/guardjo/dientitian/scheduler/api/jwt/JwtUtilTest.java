package org.github.guardjo.dientitian.scheduler.api.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {
    private final JwtUtil jwtUtil = new JwtUtil(
            new JwtProperties("test-secret-key-must-be-long-enough-for-hs256", 1800, 604800)
    );

    @DisplayName("토큰을 발급하고 유효성을 검증한다.")
    @Test
    void test_generateAccessToken_and_isValid() {
        String token = jwtUtil.generateAccessToken(1L);

        assertThat(jwtUtil.isValid(token, TokenType.ACCESS)).isTrue();
        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L);
    }

    @DisplayName("리프레시 토큰을 발급하고 유효성을 검증한다.")
    @Test
    void test_generateRefreshToken_and_isValid() {
        String token = jwtUtil.generateRefreshToken(1L);

        assertThat(jwtUtil.isValid(token, TokenType.REFRESH)).isTrue();
        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L);
    }

    @DisplayName("토큰 종류가 다르면 유효하지 않다.")
    @Test
    void test_isValid_with_different_token_type() {
        String accessToken = jwtUtil.generateAccessToken(1L);
        String refreshToken = jwtUtil.generateRefreshToken(1L);

        assertThat(jwtUtil.isValid(refreshToken, TokenType.ACCESS)).isFalse();
        assertThat(jwtUtil.isValid(accessToken, TokenType.REFRESH)).isFalse();
    }

    @DisplayName("잘못된 토큰은 유효하지 않다.")
    @Test
    void test_isValid_with_invalid_token() {
        assertThat(jwtUtil.isValid("invalid.token.value", TokenType.ACCESS)).isFalse();
    }
}
