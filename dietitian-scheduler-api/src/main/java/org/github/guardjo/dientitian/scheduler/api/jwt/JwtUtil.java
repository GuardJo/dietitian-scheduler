package org.github.guardjo.dientitian.scheduler.api.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = jwtProperties.accessTokenExpirationSeconds();
        this.refreshTokenExpirationSeconds = jwtProperties.refreshTokenExpirationSeconds();
    }

    /**
     * 주어진 인자를 기반으로 JWT 토큰을 생성한다.
     * <hr/>
     * <i>유효시간 : 30분</i>
     *
     * @param id 사용자 식별키
     * @return JWT 인증 토큰
     */
    public String generateAccessToken(Long id) {
        return generateToken(id, accessTokenExpirationSeconds);
    }

    /**
     * 주어진 인자를 기반으로 JWT 토큰을 생성한다.
     * <hr/>
     * <i>유효시간 : 7일</i>
     *
     * @param id 사용자 식별키
     * @return JWT 리프레시 토큰
     */
    public String generateRefreshToken(Long id) {
        return generateToken(id, refreshTokenExpirationSeconds);
    }

    /**
     * 인자로 주어진 JWT 토큰에서 사용자 식별키를 추출한다.
     *
     * @param token JWT 인증 토큰
     * @return 식별된 사용자 식별키
     */
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * 주어진 JWT 토큰이 유효한지 확인한다.
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String generateToken(Long id, long expirationSeconds) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(id))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
