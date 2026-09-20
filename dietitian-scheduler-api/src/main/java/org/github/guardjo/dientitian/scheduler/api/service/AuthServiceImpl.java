package org.github.guardjo.dientitian.scheduler.api.service;

import lombok.RequiredArgsConstructor;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.jwt.TokenPair;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";

    private final AccountEntityRepository accountEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public TokenPair login(String username, String password) {
        AccountEntity account = accountEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        String accessToken = jwtUtil.generateAccessToken(account.getId());
        String refreshToken = jwtUtil.generateRefreshToken(account.getId());

        return new TokenPair(accessToken, refreshToken);
    }
}
