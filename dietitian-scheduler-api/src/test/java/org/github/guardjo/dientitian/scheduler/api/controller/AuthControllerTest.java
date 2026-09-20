package org.github.guardjo.dientitian.scheduler.api.controller;

import org.github.guardjo.dientitian.scheduler.api.config.JwtConfig;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtConstant;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.LoginRequest;
import org.github.guardjo.dientitian.scheduler.api.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JwtConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-must-be-long-enough-for-hs256",
        "jwt.access-token-expiration-seconds=1800",
        "jwt.refresh-token-expiration-seconds=604800"
})
class AuthControllerTest {
    private static final String LOGIN_URL = "/api/auth/login";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @DisplayName("POST: /api/auth/login -> 정상 응답")
    @Test
    void test_login() throws Exception {
        LoginRequest request = new LoginRequest("tester", "password1!");
        TokenPair tokenPair = new TokenPair("access-token", "refresh-token");

        given(authService.login(eq(request.username()), eq(request.password()))).willReturn(tokenPair);

        BaseResponse<String> expected = BaseResponse.of(HttpStatus.OK, "Success");

        MvcResult result = mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(
                        containsString(JwtConstant.ACCESS_TOKEN_COOKIE_NAME + "=access-token"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(
                        containsString(JwtConstant.REFRESH_TOKEN_COOKIE_NAME + "=refresh-token"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("HttpOnly"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("SameSite=Strict"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("Max-Age=1800"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("Max-Age=604800"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(not(containsString("Secure")))))
                .andReturn();

        assertThat(toBaseResponse(result)).isEqualTo(expected);

        then(authService).should().login(eq(request.username()), eq(request.password()));
    }

    @DisplayName("POST: /api/auth/login -> 회원 로그인 요청 실패")
    @Test
    void test_login_failed() throws Exception {
        LoginRequest request = new LoginRequest("tester", "wrong-password");

        String message = "아이디 또는 비밀번호가 올바르지 않습니다.";
        BaseResponse<String> expected = BaseResponse.of(HttpStatus.UNAUTHORIZED, message);

        given(authService.login(eq(request.username()), eq(request.password())))
                .willThrow(new UsernameNotFoundException(message));

        MvcResult result = mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andReturn();

        assertThat(toBaseResponse(result)).isEqualTo(expected);

        then(authService).should().login(eq(request.username()), eq(request.password()));
    }

    @DisplayName("POST: /api/auth/login -> 요청 정보가 올바르지 않을 때")
    @Test
    void test_login_bad_request() throws Exception {
        LoginRequest request = new LoginRequest("", " ");

        mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    private BaseResponse<String> toBaseResponse(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(content, new TypeReference<BaseResponse<String>>() {
        });
    }
}
