package org.github.guardjo.dientitian.scheduler.api.service;

import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;

public interface AuthService {
    /**
     * 아이디, 비밀번호를 검증하고 access/refresh 토큰을 발급한다.
     *
     * @param username 사용자 아이디
     * @param password 사용자 비밀번호
     * @return 발급된 access/refresh 토큰
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException 회원 아이디에 해당하는 회원 정보를 찾지 못했을 경우
     * @throws org.springframework.security.authentication.BadCredentialsException     회원 비밀번호가 올바르지 않을 경우
     */
    TokenPair login(String username, String password);
}
