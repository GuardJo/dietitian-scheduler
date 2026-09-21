package org.github.guardjo.dientitian.scheduler.api.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.utils.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private static final String TOKEN = "access-token";
    private static final long USER_ID = 1L;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AccountEntityRepository accountEntityRepository;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("유효한 토큰이고 사용자가 존재하면, AccountUserDetails 로 인증 객체를 등록한다.")
    @Test
    void test_authenticate_existing_account() throws Exception {
        MockHttpServletRequest request = requestWithToken();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.isValid(eq(TOKEN), eq(TokenType.ACCESS))).willReturn(true);
        given(jwtUtil.getUserId(eq(TOKEN))).willReturn(USER_ID);
        given(accountEntityRepository.findById(eq(USER_ID)))
                .willReturn(Optional.of(TestDataGenerator.accountEntity(USER_ID, "tester", "테스터", "password1!")));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(new AccountUserDetails(USER_ID, "tester", "테스터"));
        verify(filterChain).doFilter(request, response);

        then(jwtUtil).should().isValid(eq(TOKEN), eq(TokenType.ACCESS));
        then(jwtUtil).should().getUserId(eq(TOKEN));
        then(accountEntityRepository).should().findById(eq(USER_ID));
    }

    @DisplayName("유효한 토큰이지만 사용자가 존재하지 않으면, 인증 객체를 등록하지 않는다.")
    @Test
    void test_not_authenticate_missing_account() throws Exception {
        MockHttpServletRequest request = requestWithToken();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.isValid(eq(TOKEN), eq(TokenType.ACCESS))).willReturn(true);
        given(jwtUtil.getUserId(eq(TOKEN))).willReturn(USER_ID);
        given(accountEntityRepository.findById(eq(USER_ID))).willReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);

        then(jwtUtil).should().isValid(eq(TOKEN), eq(TokenType.ACCESS));
        then(jwtUtil).should().getUserId(eq(TOKEN));
        then(accountEntityRepository).should().findById(eq(USER_ID));
    }

    @DisplayName("유효하지 않은 토큰이면, 사용자를 조회하지 않고 인증 객체를 등록하지 않는다.")
    @Test
    void test_not_authenticate_invalid_token() throws Exception {
        MockHttpServletRequest request = requestWithToken();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.isValid(eq(TOKEN), eq(TokenType.ACCESS))).willReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        then(jwtUtil).should().isValid(eq(TOKEN), eq(TokenType.ACCESS));
        then(accountEntityRepository).shouldHaveNoInteractions();
        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest requestWithToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(JwtConstant.ACCESS_TOKEN_COOKIE_NAME, TOKEN));

        return request;
    }
}
