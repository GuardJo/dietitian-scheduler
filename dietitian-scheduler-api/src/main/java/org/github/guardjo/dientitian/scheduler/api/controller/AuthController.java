package org.github.guardjo.dientitian.scheduler.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.controller.docs.AuthApiDocs;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtConstant;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtProperties;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.LoginRequest;
import org.github.guardjo.dientitian.scheduler.api.service.AuthService;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthApiDocs {
    private static final String PROD_PROFILE = "prod";

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final Environment environment;

    /**
     * 아이디/비밀번호를 검증하고, 발급된 access/refresh 토큰을 쿠키에 담아 내려준다.
     *
     * @param loginRequest 로그인 요청 정보 (username, password)
     * @param response     access/refresh 토큰 쿠키를 내려줄 HTTP 응답
     * @return 로그인 성공 응답
     */
    @PostMapping("/login")
    @Override
    public BaseResponse<String> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("POST : /api/auth/login, username = {}, password = {}", loginRequest.username(), "*********");
        TokenPair tokenPair = authService.login(loginRequest.username(), loginRequest.password());

        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(JwtConstant.ACCESS_TOKEN_COOKIE_NAME, tokenPair.accessToken(), JwtConstant.ACCESS_TOKEN_COOKIE_PATH, jwtProperties.accessTokenExpirationSeconds()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(JwtConstant.REFRESH_TOKEN_COOKIE_NAME, tokenPair.refreshToken(), JwtConstant.REFRESH_TOKEN_COOKIE_PATH, jwtProperties.refreshTokenExpirationSeconds()).toString());

        return BaseResponse.of(HttpStatus.OK, "Success");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void logout(@AuthenticationPrincipal AccountUserDetails userDetails, HttpServletResponse response) {
        log.info("POST : /api/auth/logout, userId = {}", userDetails.id());

        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(JwtConstant.ACCESS_TOKEN_COOKIE_NAME, "", JwtConstant.ACCESS_TOKEN_COOKIE_PATH, 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(JwtConstant.REFRESH_TOKEN_COOKIE_NAME, "", JwtConstant.REFRESH_TOKEN_COOKIE_PATH, 0).toString());
    }

    private ResponseCookie buildCookie(String name, String value, String path, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(environment.matchesProfiles(PROD_PROFILE))
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAgeSeconds)
                .build();
    }
}
