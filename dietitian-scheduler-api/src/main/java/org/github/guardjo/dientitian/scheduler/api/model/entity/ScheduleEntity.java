package org.github.guardjo.dientitian.scheduler.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "schedule", indexes = {
        @Index(name = "schedule_shift_type_id_idx", columnList = "shift_type_id"),
        @Index(name = "schedule_account_id_idx", columnList = "account_id"),
        @Index(name = "schedule_work_date_idx", columnList = "work_date"),
        @Index(name = "schedule_account_id_work_date_key", columnList = "account_id, work_date", unique = true)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ScheduleEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "id")
    private AccountEntity account;

    @Column(nullable = false)
    private LocalDate workDate;

    @ManyToOne
    @JoinColumn(name = "shift_type_id", nullable = false, referencedColumnName = "id")
    private ShiftTypeEntity shiftType;
    private String memo;
}
