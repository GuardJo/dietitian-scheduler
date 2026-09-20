package org.github.guardjo.dientitian.scheduler.api.repository;

import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.utils.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
class AccountEntityRepositoryTest {
    @Autowired
    private AccountEntityRepository accountEntityRepository;

    private final static List<AccountEntity> ACCOUNT_ENTITY_LIST = new ArrayList<>();

    @BeforeEach
    void setUp() {
        log.info("Generating test data...");
        List<AccountEntity> accountEntityList = accountEntityRepository.saveAll(List.of(
                TestDataGenerator.accountEntity("tester1", "테스터1", "password1!"),
                TestDataGenerator.accountEntity("tester2", "테스터2", "password2!")
        ));

        ACCOUNT_ENTITY_LIST.addAll(accountEntityList);
        log.info("Test data generated.");
    }

    @AfterEach
    void tearDown() {
        log.info("Deleting test data...");
        accountEntityRepository.deleteAll();
        ACCOUNT_ENTITY_LIST.clear();
        log.info("Test data deleted.");
    }

    @DisplayName("회원 아이디를 기준으로 AccountEntity 조회 성공 시")
    @Test
    void test_findByUsername() {
        AccountEntity expected = ACCOUNT_ENTITY_LIST.getFirst();
        String username = expected.getUsername();

        AccountEntity actual = accountEntityRepository.findByUsername(username).orElseThrow();

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("회원 아이디를 기준으로 AccountEntity 조회 시 찾을 수 없는 경우")
    @Test
    void test_findByUsername_not_found() {
        String username = "unknown";

        Optional<AccountEntity> actual = accountEntityRepository.findByUsername(username);

        assertThat(actual).isEmpty();
    }
}