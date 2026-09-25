package org.github.guardjo.dientitian.scheduler.api.repository;

import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.config.JpaConfig;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@Slf4j
class ShiftTypeEntityRepositoryTest {
    @Autowired
    private ShiftTypeEntityRepository shiftTypeEntityRepository;

    private final static List<ShiftTypeEntity> SHIFT_TYPE_ENTITY_LIST = new ArrayList<>();

    @BeforeEach
    void setUp() {
        log.info("Generating test data...");
        List<ShiftTypeEntity> shiftTypeEntityList = shiftTypeEntityRepository.saveAll(List.of(
                TestDataGenerator.shiftTypeEntity("C", LocalTime.of(8, 30), LocalTime.of(18, 0), "#FFAA00"),
                TestDataGenerator.shiftTypeEntity("A", LocalTime.of(5, 30), LocalTime.of(15, 0), "#00AAFF")
        ));

        SHIFT_TYPE_ENTITY_LIST.addAll(shiftTypeEntityList);
        log.info("Test data generated.");
    }

    @AfterEach
    void tearDown() {
        log.info("Deleting test data...");
        shiftTypeEntityRepository.deleteAll();
        SHIFT_TYPE_ENTITY_LIST.clear();
        log.info("Test data deleted.");
    }

    @DisplayName("라벨명을 기준으로 ShiftTypeEntity 조회 성공 시")
    @Test
    void test_findByLabel() {
        ShiftTypeEntity expected = SHIFT_TYPE_ENTITY_LIST.getFirst();
        String label = expected.getLabel();

        ShiftTypeEntity actual = shiftTypeEntityRepository.findByLabel(label).orElseThrow();

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("라벨명을 기준으로 ShiftTypeEntity 조회 시 찾을 수 없는 경우")
    @Test
    void test_findByLabel_not_found() {
        String label = "UNKNOWN";

        Optional<ShiftTypeEntity> actual = shiftTypeEntityRepository.findByLabel(label);

        assertThat(actual).isEmpty();
    }
}
