package org.github.guardjo.dientitian.scheduler.api.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtConstant {
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
}
