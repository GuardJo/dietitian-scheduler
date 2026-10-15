package org.github.guardjo.dientitian.scheduler.api.repository;

import org.github.guardjo.dientitian.scheduler.api.model.entity.PersonalEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalEventEntityRepository extends JpaRepository<PersonalEventEntity, Long> {
}
