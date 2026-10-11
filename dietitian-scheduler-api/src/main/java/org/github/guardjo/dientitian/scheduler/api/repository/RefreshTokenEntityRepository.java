package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenEntityRepository extends JpaRepository<RefreshTokenEntity, Long> {
    /**
     * 해싱처리된 리프레시 토큰에 해당하는 refresh_token Entity를 조회한다.
     *
     * @param tokenHash 해시처리된 refreshToken
     * @return refresh_token Entity
     */
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    /**
     * 해싱처리된 리프레시 토큰에 해당하는 refresh_token Entity를 삭제한다.
     *
     * @param tokenHash 해시처리된 refreshToken
     * @return 삭제된 row 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshTokenEntity rte WHERE rte.tokenHash = :tokenHash")
    long deleteAllByTokenHash(String tokenHash);

    /**
     * 사요자 식별키에 해당하는 refresh_token Entity를 삭제한다.
     *
     * @param accountId 사용자 식별키
     * @return 삭제된 row 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshTokenEntity rte WHERE rte.account.id = :accountId")
    long deleteAllByAccount_Id(Long accountId);
}
