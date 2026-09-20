package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountEntityRepository extends JpaRepository<AccountEntity, Long> {
    /**
     * 사용자 아이디에 해당하는 account Entity 를 반환한다.
     *
     * @param username 사용자 아이디
     * @return 조회된 AccountEntity
     */
    Optional<AccountEntity> findByUsername(String username);
}
