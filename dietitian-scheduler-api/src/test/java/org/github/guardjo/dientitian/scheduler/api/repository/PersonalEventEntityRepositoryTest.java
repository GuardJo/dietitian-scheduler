package org.github.guardjo.dientitian.scheduler.api.repository;

import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.config.JpaConfig;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.PersonalEventEntity;
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
class PersonalEventEntityRepositoryTest {
    private static final Sort SORT = Sort.by(Sort.Order.asc("eventDate"), Sort.Order.asc("startTime"));

    @Autowired
    private PersonalEventEntityRepository personalEventEntityRepository;

    @Autowired
    private AccountEntityRepository accountEntityRepository;

    private AccountEntity account;
    private AccountEntity otherAccount;

    @BeforeEach
    void setUp() {
        log.info("Generating test data...");
        account = accountEntityRepository.save(TestDataGenerator.accountEntity("tester1", "테스터1", "password1!"));
        otherAccount = accountEntityRepository.save(TestDataGenerator.accountEntity("tester2", "테스터2", "password2!"));
        log.info("Test data generated.");
    }

    @AfterEach
    void tearDown() {
        log.info("Deleting test data...");
        personalEventEntityRepository.deleteAll();
        accountEntityRepository.deleteAll();
        log.info("Test data deleted.");
    }

    @DisplayName("주어진 기간 내에 속한 사용자의 개인 일정 목록을 조회한다.")
    @Test
    void test_findAllByAccountIdAndEventDateBetween() {
        String title1 = "시작일";
        String title2 = "중간";
        String title3 = "종료일";
        personalEventEntityRepository.saveAll(List.of(
                TestDataGenerator.personalEventEntity(account, "기간 이전", LocalDate.of(2026, 8, 31), null, null),
                TestDataGenerator.personalEventEntity(account, title1, LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(10, 0)),
                TestDataGenerator.personalEventEntity(account, title2, LocalDate.of(2026, 9, 15), null, null),
                TestDataGenerator.personalEventEntity(account, title3, LocalDate.of(2026, 9, 30), LocalTime.of(13, 0), LocalTime.of(14, 0)),
                TestDataGenerator.personalEventEntity(account, "기간 이후", LocalDate.of(2026, 10, 1), null, null),
                TestDataGenerator.personalEventEntity(otherAccount, "다른 사용자", LocalDate.of(2026, 9, 15), null, null)
        ));

        List<PersonalEventEntity> events = personalEventEntityRepository.findAllByAccountIdAndEventDateBetween(
                account.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), SORT);

        assertThat(events)
                .allSatisfy(event -> assertThat(event.getAccount().getId()).isEqualTo(account.getId()))
                .extracting(PersonalEventEntity::getTitle)
                .containsExactlyInAnyOrder(title1, title2, title3);
    }

    @DisplayName("조회된 개인 일정 목록은 주어진 정렬 조건에 따라 정렬된다.")
    @Test
    void test_findAllByAccountIdAndEventDateBetween_sorted() {
        String title1 = "9/10";
        String title2 = "9/20 오전";
        String title3 = "9/30 오후";
        personalEventEntityRepository.saveAll(List.of(
                TestDataGenerator.personalEventEntity(account, title3, LocalDate.of(2026, 9, 20), LocalTime.of(15, 0), LocalTime.of(16, 0)),
                TestDataGenerator.personalEventEntity(account, title1, LocalDate.of(2026, 9, 10), LocalTime.of(10, 0), LocalTime.of(11, 0)),
                TestDataGenerator.personalEventEntity(account, title2, LocalDate.of(2026, 9, 20), LocalTime.of(9, 0), LocalTime.of(10, 0))
        ));

        List<PersonalEventEntity> events = personalEventEntityRepository.findAllByAccountIdAndEventDateBetween(
                account.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), SORT);

        assertThat(events)
                .extracting(PersonalEventEntity::getTitle)
                .containsExactly(title1, title2, title3);
    }

    @DisplayName("사용자의 개인 일정이 존재하지만 주어진 기간에 해당하지 않으면 빈 목록을 반환한다.")
    @Test
    void test_findAllByAccountIdAndEventDateBetween_out_of_range() {
        personalEventEntityRepository.saveAll(List.of(
                TestDataGenerator.personalEventEntity(account, "기간 이전", LocalDate.of(2026, 8, 31), null, null),
                TestDataGenerator.personalEventEntity(account, "기간 이후", LocalDate.of(2026, 10, 1), null, null)
        ));

        List<PersonalEventEntity> events = personalEventEntityRepository.findAllByAccountIdAndEventDateBetween(
                account.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), SORT);

        assertThat(events).isEmpty();
    }

    @DisplayName("사용자의 개인 일정이 존재하지 않으면 빈 목록을 반환한다.")
    @Test
    void test_findAllByAccountIdAndEventDateBetween_no_data() {
        personalEventEntityRepository.save(
                TestDataGenerator.personalEventEntity(otherAccount, "다른 사용자", LocalDate.of(2026, 9, 15), null, null));

        List<PersonalEventEntity> events = personalEventEntityRepository.findAllByAccountIdAndEventDateBetween(
                account.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), SORT);

        assertThat(events).isEmpty();
    }
}
