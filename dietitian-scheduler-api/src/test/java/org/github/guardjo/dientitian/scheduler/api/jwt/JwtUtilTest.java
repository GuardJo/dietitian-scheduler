package org.github.guardjo.dientitian.scheduler.api.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {
    private final JwtUtil jwtUtil = new JwtUtil(
            new JwtProperties("test-secret-key-must-be-long-enough-for-hs256", 1800, 604800)
    );

    @DisplayName("토큰을 방급하고 유효성을 검증한다.")
    @Test
    void test_generateAccessToken_and_isValid() {
        String token = jwtUtil.generateAccessToken(1L);

        assertThat(jwtUtil.isValid(token)).isTrue();
        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L);
    }

    @DisplayName("잘못된 토큰은 유효하지 않다.")
    @Test
    void test_isValid_with_invalid_token() {
        assertThat(jwtUtil.isValid("invalid.token.value")).isFalse();
    }
}
