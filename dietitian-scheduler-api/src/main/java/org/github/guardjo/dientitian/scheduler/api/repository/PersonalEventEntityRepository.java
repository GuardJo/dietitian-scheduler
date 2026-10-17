package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.PersonalEventEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PersonalEventEntityRepository extends JpaRepository<PersonalEventEntity, Long> {
    /**
     * 특정 사용자에 대해 주어진 기간 개 personal_event 목록을 반환한다.
     *
     * @param accountId 사용자 식별키
     * @param startDate 조회 기간 시작 일자
     * @param endDate   조회 기간 종료 일자
     * @param sort      정렬 조건
     * @return 해당하는 기간 내 personal_event 목록
     */
    List<PersonalEventEntity> findAllByAccountIdAndEventDateBetween(Long accountId, LocalDate startDate, LocalDate endDate, Sort sort);
}
