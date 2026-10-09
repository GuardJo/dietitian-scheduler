package org.github.guardjo.dientitian.scheduler.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenType;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.RefreshTokenEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.repository.RefreshTokenEntityRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";
    private static final String HASH_ALGORITHM_TYPE = "SHA-256";

    private final AccountEntityRepository accountEntityRepository;
    private final RefreshTokenEntityRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    @Override
    public TokenPair login(String username, String password) {
        AccountEntity account = accountEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        String accessToken = jwtUtil.generateAccessToken(account.getId());
        String refreshToken = jwtUtil.generateRefreshToken(account.getId());

        createRefreshToken(account, refreshToken);

        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    @Override
    public TokenPair refresh(String refreshToken) {
        log.info("Refreshing token");

        if (!jwtUtil.isValid(refreshToken, TokenType.REFRESH)) {
            log.warn("Invalid refresh token: {}", refreshToken);
            throw new BadCredentialsException("인증 정보가 올바르지 않습니다.");
        }

        String tokenHash = sha256Hex(refreshToken);

        // TODO 조회 결과가 없을 시 해당 계정의 리프레시 토큰들 제거 (토큰 탈취 당했을 가능성)
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("인증 정보가 존재하지 않습니다"));

        Long userId = refreshTokenEntity.getAccount().getId();
        String newAccessToken = jwtUtil.generateAccessToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        refreshTokenEntity.setExpiredAt(calcExpiredAt());
        refreshTokenEntity.setTokenHash(sha256Hex(newRefreshToken));

        log.info("Refresh token, userId: {}", userId);
        return new TokenPair(newAccessToken, newRefreshToken);
    }

    /*
    SHA-256 해싱 처리 및 16진수 변환
     */
    private String sha256Hex(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM_TYPE);
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.warn("Failed hashing token, {}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /*
    토큰 유효일시 갱신 처리
     */
    private LocalDateTime calcExpiredAt() {
        return LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpirationSeconds());
    }

    /*
    신규 refresh token 저장
     */
    private void createRefreshToken(AccountEntity account, String refreshToken) {
        log.info("Creating new refresh token");
        RefreshTokenEntity newRefreshToken = refreshTokenRepository.save(RefreshTokenEntity.builder()
                .account(account)
                .expiredAt(calcExpiredAt())
                .tokenHash(sha256Hex(refreshToken))
                .build());

        log.info("New refresh token created, id: {}", newRefreshToken.getId());
    }
}
