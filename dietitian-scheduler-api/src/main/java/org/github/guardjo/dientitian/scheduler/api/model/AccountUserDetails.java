package org.github.guardjo.dientitian.scheduler.api.model;

import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT 인증 후 SecurityContext 에 등록되는 인증된 사용자 정보.
 * 토큰 기반 인증이므로 비밀번호는 보관하지 않는다.
 */
public record AccountUserDetails(
        Long id,
        String username,
        String name,
        String encryptedPassword
) implements UserDetails {
    public static AccountUserDetails from(AccountEntity account) {
        return new AccountUserDetails(account.getId(), account.getUsername(), account.getName(), account.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.encryptedPassword;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
