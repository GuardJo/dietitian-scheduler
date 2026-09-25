package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.ShiftTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShiftTypeEntityRepository extends JpaRepository<ShiftTypeEntity, Long> {
    /**
     * 인자로 주어진 라벨명에 해당하는 근무 타입 정보를 반환한다.
     *
     * @param label 라벨명
     * @return 라벨명에 해당하는 근무 타입 정보
     */
    Optional<ShiftTypeEntity> findByLabel(String label);
}
