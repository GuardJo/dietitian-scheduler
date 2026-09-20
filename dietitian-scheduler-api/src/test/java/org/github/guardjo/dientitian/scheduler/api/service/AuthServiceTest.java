package org.github.guardjo.dientitian.scheduler.api.service;

import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.utils.TestDataGenerator;
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

    @InjectMocks
    private AuthServiceImpl authService;

    @DisplayName("아이디, 비밀번호가 일치하면 토큰을 발급한다.")
    @Test
    void test_login_success() {
        String username = "tester";
        String password = "password1!";
        TokenPair expected = new TokenPair("access-token", "refresh-token");

        AccountEntity account = TestDataGenerator.accountEntity(1L, "tester", "테스터", "encoded-password");

        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.of(account));
        given(passwordEncoder.matches(eq(password), eq(account.getPassword()))).willReturn(true);
        given(jwtUtil.generateAccessToken(eq(account.getId()))).willReturn(expected.accessToken());
        given(jwtUtil.generateRefreshToken(eq(account.getId()))).willReturn(expected.refreshToken());

        TokenPair actual = authService.login(username, password);

        assertThat(actual).isEqualTo(expected);

        then(accountEntityRepository).should().findByUsername(eq(username));
        then(passwordEncoder).should().matches(eq(password), eq(account.getPassword()));
        then(jwtUtil).should().generateAccessToken(eq(account.getId()));
        then(jwtUtil).should().generateRefreshToken(eq(account.getId()));
    }

    @DisplayName("존재하지 않는 아이디로 로그인하면 예외가 발생한다.")
    @Test
    void test_login_with_unknown_username() {
        String username = "unknown";
        given(accountEntityRepository.findByUsername(eq(username))).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(username, "password1!"))
                .isInstanceOf(UsernameNotFoundException.class);

        then(accountEntityRepository).should().findByUsername(eq(username));
        verifyNoInteractions(jwtUtil);
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
        verifyNoInteractions(jwtUtil);
    }
}
