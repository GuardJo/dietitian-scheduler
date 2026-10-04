package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.ScheduleEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleEntityRepository extends JpaRepository<ScheduleEntity, Long> {
    /**
     * 주어진 사용자 식별키에 해당하는 사용자의 스케줄 항목 중 주어진 기간에 해당하는 스케줄을 삭제한다.
     *
     * @param startDate 삭제 기한 시작 일자
     * @param endDate   삭제 기한 종료 일자
     * @param accountId 사용자 식별키
     * @return 삭제된 스케줄의 수
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ScheduleEntity s where s.account.id = :accountId and s.workDate between :startDate and :endDate")
    int deleteAllByWorkDateBetweenAndAccount_Id(LocalDate startDate, LocalDate endDate, Long accountId);

    /**
     * 주어진 사용자 식별키에 해당하는 사용자의 스케줄 중 해당 기간에 포함되는 스케줄들을 조회한다.
     *
     * @param accountId      사용자 식별키
     * @param workDateAfter  조회 기간 시작 일자
     * @param workDateBefore 조회 기간 끝 일자
     * @param sort           정렬 조건
     * @return 해당하는 근무 스케줄 목록
     */
    List<ScheduleEntity> findAllByAccount_IdAndWorkDateBetween(Long accountId, LocalDate workDateAfter, LocalDate workDateBefore, Sort sort);
}
