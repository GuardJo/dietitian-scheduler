package org.github.guardjo.dientitian.scheduler.api.repository;

import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.config.JpaConfig;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ScheduleEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ShiftTypeEntity;
import org.github.guardjo.dientitian.scheduler.api.utils.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@Slf4j
class ScheduleEntityRepositoryTest {
    @Autowired
    private ScheduleEntityRepository scheduleEntityRepository;

    @Autowired
    private AccountEntityRepository accountEntityRepository;

    @Autowired
    private ShiftTypeEntityRepository shiftTypeEntityRepository;

    private AccountEntity account;
    private AccountEntity otherAccount;
    private ShiftTypeEntity shiftType;

    @BeforeEach
    void setUp() {
        log.info("Generating test data...");
        account = accountEntityRepository.save(TestDataGenerator.accountEntity("tester1", "테스터1", "password1!"));
        otherAccount = accountEntityRepository.save(TestDataGenerator.accountEntity("tester2", "테스터2", "password2!"));
        shiftType = shiftTypeEntityRepository.save(TestDataGenerator.shiftTypeEntity("C", LocalTime.of(8, 30), LocalTime.of(18, 0), "#FFAA00"));
        log.info("Test data generated.");
    }

    @AfterEach
    void tearDown() {
        log.info("Deleting test data...");
        scheduleEntityRepository.deleteAll();
        shiftTypeEntityRepository.deleteAll();
        accountEntityRepository.deleteAll();
        log.info("Test data deleted.");
    }

    @DisplayName("주어진 기간 내에 속한 사용자의 스케줄만 삭제한다.")
    @Test
    void test_deleteAllByWorkDateBetweenAndAccount_Id() {
        scheduleEntityRepository.saveAll(List.of(
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 9, 1), shiftType),
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 9, 15), shiftType),
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 9, 30), shiftType)
        ));

        int deletedCount = scheduleEntityRepository.deleteAllByWorkDateBetweenAndAccount_Id(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15), account.getId());

        assertThat(deletedCount).isEqualTo(2);
        assertThat(scheduleEntityRepository.findAll())
                .extracting(ScheduleEntity::getWorkDate)
                .containsExactly(LocalDate.of(2026, 9, 30));
    }

    @DisplayName("주어진 기간을 벗어난 스케줄은 삭제되지 않는다.")
    @Test
    void test_deleteAllByWorkDateBetweenAndAccount_Id_out_of_range() {
        scheduleEntityRepository.save(TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 10, 1), shiftType));

        int deletedCount = scheduleEntityRepository.deleteAllByWorkDateBetweenAndAccount_Id(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), account.getId());

        assertThat(deletedCount).isZero();
        assertThat(scheduleEntityRepository.findAll()).hasSize(1);
    }

    @DisplayName("다른 사용자의 스케줄은 삭제되지 않는다.")
    @Test
    void test_deleteAllByWorkDateBetweenAndAccount_Id_other_account() {
        scheduleEntityRepository.saveAll(List.of(
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 9, 10), shiftType),
                TestDataGenerator.scheduleEntity(otherAccount, LocalDate.of(2026, 9, 10), shiftType)
        ));

        int deletedCount = scheduleEntityRepository.deleteAllByWorkDateBetweenAndAccount_Id(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), account.getId());

        assertThat(deletedCount).isEqualTo(1);
        assertThat(scheduleEntityRepository.findAll())
                .extracting(entity -> entity.getAccount().getId())
                .containsExactly(otherAccount.getId());
    }

    @DisplayName("삭제 대상 스케줄이 없으면 0을 반환한다.")
    @Test
    void test_deleteAllByWorkDateBetweenAndAccount_Id_no_data() {
        int deletedCount = scheduleEntityRepository.deleteAllByWorkDateBetweenAndAccount_Id(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), account.getId());

        assertThat(deletedCount).isZero();
    }

    @DisplayName("주어진 기간 내에 속한 사용자의 스케줄 목록을 조회한다.")
    @Test
    void test_findAllByAccount_IdAndWorkDateBetween() {
        scheduleEntityRepository.saveAll(List.of(
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 8, 31), shiftType),
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 9, 1), shiftType),
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 9, 15), shiftType),
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 9, 30), shiftType),
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 10, 1), shiftType),
                TestDataGenerator.scheduleEntity(otherAccount, LocalDate.of(2026, 9, 15), shiftType)
        ));

        List<ScheduleEntity> schedules = scheduleEntityRepository.findAllByAccount_IdAndWorkDateBetween(
                account.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                Sort.by(Sort.Direction.ASC, "workDate"));

        assertThat(schedules)
                .allSatisfy(schedule -> assertThat(schedule.getAccount().getId()).isEqualTo(account.getId()))
                .extracting(ScheduleEntity::getWorkDate)
                .containsExactlyInAnyOrder(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 15),
                        LocalDate.of(2026, 9, 30)
                );
    }

    @DisplayName("사용자의 스케줄이 존재하지만 주어진 기간에 해당하지 않으면 빈 목록을 반환한다.")
    @Test
    void test_findAllByAccount_IdAndWorkDateBetween_out_of_range() {
        scheduleEntityRepository.saveAll(List.of(
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 8, 31), shiftType),
                TestDataGenerator.scheduleEntity(account, LocalDate.of(2026, 10, 1), shiftType)
        ));

        List<ScheduleEntity> schedules = scheduleEntityRepository.findAllByAccount_IdAndWorkDateBetween(
                account.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                Sort.by(Sort.Direction.ASC, "workDate"));

        assertThat(schedules).isEmpty();
    }

    @DisplayName("사용자의 스케줄이 존재하지 않으면 빈 목록을 반환한다.")
    @Test
    void test_findAllByAccount_IdAndWorkDateBetween_no_data() {
        scheduleEntityRepository.save(TestDataGenerator.scheduleEntity(otherAccount, LocalDate.of(2026, 9, 15), shiftType));

        List<ScheduleEntity> schedules = scheduleEntityRepository.findAllByAccount_IdAndWorkDateBetween(
                account.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                Sort.by(Sort.Direction.ASC, "workDate"));

        assertThat(schedules).isEmpty();
    }
}
