package org.github.guardjo.dientitian.scheduler.api.jwt;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
