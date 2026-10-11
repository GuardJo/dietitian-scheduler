package org.github.guardjo.dientitian.scheduler.api.service;

import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenType;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.RefreshTokenEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.repository.RefreshTokenEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.utils.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AccountEntityRepository accountEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenEntityRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @DisplayName("아이디, 비밀번호가 일치하면 토큰을 발급한다.")
    @Test
    void test_login_success() {
        String username = "tester";
        String password = "password1!";
        TokenPair expected = new TokenPair("access-token", "refresh-token");

        AccountEntity account = TestDataGenerator.accountEntity(1L, "tester", "테스터", "encoded-password");
        RefreshTokenEntity refreshTokenEntity = TestDataGenerator.refreshTokenEntity(1L, account, "token-hash", LocalDateTime.now().plusDays(7));

        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.of(account));
        given(passwordEncoder.matches(eq(password), eq(account.getPassword()))).willReturn(true);
        given(jwtUtil.generateAccessToken(eq(account.getId()))).willReturn(expected.accessToken());
        given(jwtUtil.generateRefreshToken(eq(account.getId()))).willReturn(expected.refreshToken());
        given(jwtUtil.getRefreshTokenExpirationSeconds()).willReturn(604800L);
        given(refreshTokenRepository.save(any(RefreshTokenEntity.class))).willReturn(refreshTokenEntity);

        TokenPair actual = authService.login(username, password);

        assertThat(actual).isEqualTo(expected);

        then(accountEntityRepository).should().findByUsername(eq(username));
        then(passwordEncoder).should().matches(eq(password), eq(account.getPassword()));
        then(jwtUtil).should().generateAccessToken(eq(account.getId()));
        then(jwtUtil).should().generateRefreshToken(eq(account.getId()));
        then(refreshTokenRepository).should().save(any(RefreshTokenEntity.class));
    }

    @DisplayName("존재하지 않는 아이디로 로그인하면 예외가 발생한다.")
    @Test
    void test_login_with_unknown_username() {
        String username = "unknown";
        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(username, "password1!"))
                .isInstanceOf(UsernameNotFoundException.class);

        then(accountEntityRepository).should().findByUsername(eq(username));
        verifyNoInteractions(jwtUtil, refreshTokenRepository, passwordEncoder);
    }

    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void test_login_with_wrong_password() {
        String username = "tester";
        String password = "wrong-password";
        AccountEntity account = TestDataGenerator.accountEntity("tester", "테스터", "encoded-password");

        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.of(account));
        given(passwordEncoder.matches(eq(password), eq(account.getPassword()))).willReturn(false);

        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(BadCredentialsException.class);

        then(accountEntityRepository).should().findByUsername(eq(username));
        then(passwordEncoder).should().matches(eq(password), eq(account.getPassword()));
        verifyNoInteractions(jwtUtil, refreshTokenRepository);
    }

    @DisplayName("유효한 리프레시 토큰으로 토큰을 갱신한다.")
    @Test
    void test_refresh_success() {
        String refreshToken = "valid-refresh-token";
        String tokenHash = "abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890abcdef12";
        AccountEntity account = TestDataGenerator.accountEntity(1L, "tester", "테스터", "encoded-password");
        RefreshTokenEntity refreshTokenEntity = TestDataGenerator.refreshTokenEntity(
                account,
                tokenHash,
                LocalDateTime.now().plusDays(7)
        );

        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        TokenPair expected = new TokenPair(newAccessToken, newRefreshToken);

        given(jwtUtil.isValid(eq(refreshToken), eq(TokenType.REFRESH))).willReturn(true);
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(refreshTokenEntity));
        given(jwtUtil.generateAccessToken(eq(account.getId()))).willReturn(newAccessToken);
        given(jwtUtil.generateRefreshToken(eq(account.getId()))).willReturn(newRefreshToken);
        given(jwtUtil.getRefreshTokenExpirationSeconds()).willReturn(604800L);

        TokenPair actual = authService.refresh(refreshToken);

        assertThat(actual).isEqualTo(expected);

        then(jwtUtil).should().isValid(eq(refreshToken), eq(TokenType.REFRESH));
        then(refreshTokenRepository).should().findByTokenHash(anyString());
        then(jwtUtil).should().generateAccessToken(eq(account.getId()));
        then(jwtUtil).should().generateRefreshToken(eq(account.getId()));
    }

    @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 예외가 발생한다.")
    @Test
    void test_refresh_with_invalid_token() {
        String invalidRefreshToken = "invalid-refresh-token";

        given(jwtUtil.isValid(eq(invalidRefreshToken), eq(TokenType.REFRESH))).willReturn(false);

        assertThatThrownBy(() -> authService.refresh(invalidRefreshToken))
                .isInstanceOf(BadCredentialsException.class);

        then(jwtUtil).should().isValid(eq(invalidRefreshToken), eq(TokenType.REFRESH));
        verifyNoInteractions(refreshTokenRepository);
    }

    @DisplayName("DB에 존재하지 않는 리프레시 토큰으로 갱신 시 예외가 발생한다.")
    @Test
    void test_refresh_with_not_found_token() {
        String refreshToken = "valid-but-not-stored-token";
        Long userId = 1L;

        given(jwtUtil.isValid(eq(refreshToken), eq(TokenType.REFRESH))).willReturn(true);
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.empty());
        given(jwtUtil.getUserId(eq(refreshToken))).willReturn(userId);
        given(refreshTokenRepository.deleteAllByAccount_Id(eq(userId))).willReturn(100L);

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);

        then(jwtUtil).should().isValid(eq(refreshToken), eq(TokenType.REFRESH));
        then(refreshTokenRepository).should().findByTokenHash(anyString());
        then(refreshTokenRepository).should().deleteAllByAccount_Id(eq(userId));
    }

    @DisplayName("로그아웃 시 해당 리프레시 토큰을 삭제한다.")
    @Test
    void test_logout_success() {
        String refreshToken = "valid-refresh-token";

        given(refreshTokenRepository.deleteAllByTokenHash(anyString())).willReturn(1L);

        authService.logout(refreshToken);

        then(refreshTokenRepository).should().deleteAllByTokenHash(anyString());
    }

    @DisplayName("존재하지 않는 리프레시 토큰으로 로그아웃 시 정상 처리된다.")
    @Test
    void test_logout_with_non_existing_token() {
        String refreshToken = "non-existing-token";

        given(refreshTokenRepository.deleteAllByTokenHash(anyString())).willReturn(0L);

        authService.logout(refreshToken);

        then(refreshTokenRepository).should().deleteAllByTokenHash(anyString());
    }
}
