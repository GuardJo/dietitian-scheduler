package org.github.guardjo.dientitian.scheduler.api.repository;

import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.config.JpaConfig;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.RefreshTokenEntity;
import org.github.guardjo.dientitian.scheduler.api.utils.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@Slf4j
class RefreshTokenEntityRepositoryTest {
    @Autowired
    private RefreshTokenEntityRepository refreshTokenRepository;

    @Autowired
    private AccountEntityRepository accountEntityRepository;

    private AccountEntity testAccount;
    private RefreshTokenEntity testRefreshToken;

    @BeforeEach
    void setUp() {
        log.info("Generating test data...");
        testAccount = accountEntityRepository.save(
                TestDataGenerator.accountEntity("testUser", "테스트유저", "password123!")
        );
        testRefreshToken = refreshTokenRepository.save(
                TestDataGenerator.refreshTokenEntity(
                        testAccount,
                        "abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890abcdef12",
                        LocalDateTime.now().plusDays(7)
                )
        );
        log.info("Test data generated.");
    }

    @AfterEach
    void tearDown() {
        log.info("Deleting test data...");
        refreshTokenRepository.deleteAll();
        accountEntityRepository.deleteAll();
        log.info("Test data deleted.");
    }

    @DisplayName("토큰 해시값을 기준으로 RefreshTokenEntity 조회 성공 시")
    @Test
    void test_findByTokenHash() {
        String tokenHash = testRefreshToken.getTokenHash();

        RefreshTokenEntity actual = refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow();

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(testRefreshToken.getId());
        assertThat(actual.getTokenHash()).isEqualTo(tokenHash);
        assertThat(actual.getAccount().getId()).isEqualTo(testAccount.getId());
    }

    @DisplayName("토큰 해시값을 기준으로 RefreshTokenEntity 조회 시 찾을 수 없는 경우")
    @Test
    void test_findByTokenHash_not_found() {
        String unknownTokenHash = "unknown123456789012345678901234567890123456789012345678901234";

        Optional<RefreshTokenEntity> actual = refreshTokenRepository.findByTokenHash(unknownTokenHash);

        assertThat(actual).isEmpty();
    }
}