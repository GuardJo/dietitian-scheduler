package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenEntityRepository extends JpaRepository<RefreshTokenEntity, Long> {
    /**
     * 해싱처리된 리프레시 토큰에 해당하는 refresh_token Entity를 조회한다.
     *
     * @param tokenHash 해시처리된 refreshToken
     * @return refresh_token Entity
     */
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
}
