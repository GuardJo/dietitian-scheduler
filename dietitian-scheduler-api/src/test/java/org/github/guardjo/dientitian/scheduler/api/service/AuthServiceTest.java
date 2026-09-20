package org.github.guardjo.dientitian.scheduler.api.service;

import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AccountEntityRepository accountEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @DisplayName("아이디, 비밀번호가 일치하면 토큰을 발급한다.")
    @Test
    void test_login_success() {
        String username = "tester";
        String password = "password1!";
        TokenPair expected = new TokenPair("access-token", "refresh-token");

        AccountEntity account = AccountEntity.builder()
                .id(1L)
                .username(username)
                .password("encoded-password")
                .name("테스터")
                .build();

        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.of(account));
        given(passwordEncoder.matches(eq(password), eq(account.getPassword()))).willReturn(true);
        given(jwtUtil.generateAccessToken(1L)).willReturn(expected.accessToken());
        given(jwtUtil.generateRefreshToken(1L)).willReturn(expected.refreshToken());

        TokenPair actual = authService.login(username, password);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("존재하지 않는 아이디로 로그인하면 예외가 발생한다.")
    @Test
    void test_login_with_unknown_username() {
        String username = "unknown";
        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(username, "password1!"))
                .isInstanceOf(UsernameNotFoundException.class);

        verifyNoInteractions(jwtUtil);
    }

    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void test_login_with_wrong_password() {
        String username = "tester";
        String password = "wrong-password";
        AccountEntity account = AccountEntity.builder()
                .id(1L)
                .username("tester")
                .password("encoded-password")
                .name("테스터")
                .build();

        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.of(account));
        given(passwordEncoder.matches(eq(password), eq(account.getPassword()))).willReturn(false);

        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtUtil);
    }
}
