package org.github.guardjo.dientitian.scheduler.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "personal_event", indexes = {
        @Index(name = "personal_event_account_id_event_date_idx", columnList = "account_id,eventDate")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class PersonalEventEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "id")
    private AccountEntity account;

    @Column(nullable = false, length = 60)
    private String title;

    @Column(length = 300)
    private String memo;

    @Column(nullable = false)
    private LocalDate eventDate;
    
    private LocalTime startTime;
    private LocalTime endTime;
}
