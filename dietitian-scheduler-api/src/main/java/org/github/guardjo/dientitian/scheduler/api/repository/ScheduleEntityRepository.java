package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleEntityRepository extends JpaRepository<ScheduleEntity, Long> {
}
