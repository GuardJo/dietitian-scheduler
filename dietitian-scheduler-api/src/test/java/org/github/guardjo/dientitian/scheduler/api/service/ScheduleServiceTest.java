package org.github.guardjo.dientitian.scheduler.api.service;

import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.dto.DailyShift;
import org.github.guardjo.dientitian.scheduler.api.model.dto.MonthScheduleData;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ScheduleEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ShiftTypeEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.repository.ScheduleEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.repository.ShiftTypeEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.util.ExcelScheduleParser;
import org.github.guardjo.dientitian.scheduler.api.utils.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
    private static final AccountUserDetails USER_DETAILS = new AccountUserDetails(1L, "tester", "테스터", "password");
    private static final int YEAR = 2026;
    private static final int MONTH = 9;
    private static final LocalDate START_DATE = LocalDate.of(YEAR, MONTH, 1);
    private static final LocalDate END_DATE = START_DATE.plusMonths(1).minusDays(1);
    private static final MultipartFile EXCEL_FILE = new MockMultipartFile("file", "schedule.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1, 2, 3});
    private static final Sort WORK_DATE_ASC = Sort.by(Sort.Direction.ASC, "workDate");

    @Mock
    private AccountEntityRepository accountRepository;

    @Mock
    private ExcelScheduleParser excelScheduleParser;

    @Mock
    private ShiftTypeEntityRepository shiftTypeRepository;

    @Mock
    private ScheduleEntityRepository scheduleRepository;

    @Captor
    private ArgumentCaptor<List<ScheduleEntity>> scheduleEntitiesCaptor;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @DisplayName("정상적으로 엑셀 파일을 파싱하여 스케줄을 저장한다.")
    @Test
    void test_saveShiftSchedules_success() {
        AccountEntity account = accountEntity();
        ShiftTypeEntity dayShift = TestDataGenerator.shiftTypeEntity("C", LocalTime.of(8, 30), LocalTime.of(18, 0), "#FFAA00");
        ShiftTypeEntity nightShift = TestDataGenerator.shiftTypeEntity("A", LocalTime.of(5, 30), LocalTime.of(15, 0), "#00AAFF");

        given(accountRepository.existsById(eq(USER_DETAILS.id()))).willReturn(true);
        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);
        given(scheduleRepository.deleteAllByWorkDateBetweenAndAccount_Id(eq(START_DATE), eq(END_DATE), eq(account.getId()))).willReturn(0);
        given(shiftTypeRepository.findAll()).willReturn(List.of(dayShift, nightShift));
        given(excelScheduleParser.parse(eq(EXCEL_FILE), eq(account.getName()))).willReturn(List.of(
                new DailyShift(1, "C"),
                new DailyShift(2, "A"),
                new DailyShift(3, "UNKNOWN") // 일치하는 근무타입이 없어 저장 대상에서 제외되어야 함
        ));

        assertThatCode(() -> scheduleService.saveShiftSchedules(USER_DETAILS, YEAR, MONTH, EXCEL_FILE))
                .doesNotThrowAnyException();

        then(accountRepository).should().existsById(eq(USER_DETAILS.id()));
        then(accountRepository).should().getReferenceById(eq(USER_DETAILS.id()));
        then(scheduleRepository).should().deleteAllByWorkDateBetweenAndAccount_Id(eq(START_DATE), eq(END_DATE), eq(account.getId()));
        then(shiftTypeRepository).should().findAll();
        then(excelScheduleParser).should().parse(eq(EXCEL_FILE), eq(account.getName()));
        then(scheduleRepository).should().saveAll(scheduleEntitiesCaptor.capture());
        List<ScheduleEntity> saved = scheduleEntitiesCaptor.getValue();

        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(ScheduleEntity::getWorkDate)
                .containsExactly(LocalDate.of(YEAR, MONTH, 1), LocalDate.of(YEAR, MONTH, 2));
        assertThat(saved).extracting(ScheduleEntity::getShiftType)
                .containsExactly(dayShift, nightShift);
        assertThat(saved).extracting(ScheduleEntity::getAccount)
                .containsOnly(account);
    }

    @DisplayName("회원 정보를 찾을 수 없으면 예외가 발생한다.")
    @Test
    void test_saveShiftSchedules_account_not_found() {
        given(accountRepository.existsById(eq(USER_DETAILS.id()))).willReturn(false);

        assertThatThrownBy(() -> scheduleService.saveShiftSchedules(USER_DETAILS, YEAR, MONTH, EXCEL_FILE))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("해당 사용자 정보를 찾을 수 없습니다.");

        then(accountRepository).should().existsById(eq(USER_DETAILS.id()));
        verifyNoInteractions(excelScheduleParser, shiftTypeRepository, scheduleRepository);
    }

    @DisplayName("엑셀 파일에서 스케줄 데이터를 추출하는 중 오류가 발생하면 예외가 발생한다.")
    @Test
    void test_saveShiftSchedules_excel_parse_error() {
        AccountEntity account = accountEntity();

        given(accountRepository.existsById(eq(USER_DETAILS.id()))).willReturn(true);
        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);
        given(scheduleRepository.deleteAllByWorkDateBetweenAndAccount_Id(eq(START_DATE), eq(END_DATE), eq(account.getId()))).willReturn(0);
        given(excelScheduleParser.parse(eq(EXCEL_FILE), eq(account.getName())))
                .willThrow(new IllegalArgumentException("사용자의 스케줄 정보가 확인되지 않습니다."));

        assertThatThrownBy(() -> scheduleService.saveShiftSchedules(USER_DETAILS, YEAR, MONTH, EXCEL_FILE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자의 스케줄 정보가 확인되지 않습니다.");

        then(accountRepository).should().existsById(eq(USER_DETAILS.id()));
        then(accountRepository).should().getReferenceById(eq(USER_DETAILS.id()));
        then(scheduleRepository).should().deleteAllByWorkDateBetweenAndAccount_Id(eq(START_DATE), eq(END_DATE), eq(account.getId()));
        then(excelScheduleParser).should().parse(eq(EXCEL_FILE), eq(account.getName()));
        verifyNoMoreInteractions(scheduleRepository);
    }

    @DisplayName("스케줄 데이터를 저장하는 중 오류가 발생하면 예외가 발생한다.")
    @Test
    void test_saveShiftSchedules_save_error() {
        AccountEntity account = accountEntity();
        ShiftTypeEntity dayShift = TestDataGenerator.shiftTypeEntity("C", LocalTime.of(8, 30), LocalTime.of(18, 0), "#FFAA00");

        given(accountRepository.existsById(eq(USER_DETAILS.id()))).willReturn(true);
        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);
        given(scheduleRepository.deleteAllByWorkDateBetweenAndAccount_Id(eq(START_DATE), eq(END_DATE), eq(account.getId()))).willReturn(0);
        given(shiftTypeRepository.findAll()).willReturn(List.of(dayShift));
        given(excelScheduleParser.parse(eq(EXCEL_FILE), eq(account.getName()))).willReturn(List.of(new DailyShift(1, "C")));
        given(scheduleRepository.saveAll(anyList())).willThrow(new DataIntegrityViolationException("저장 실패"));

        assertThatThrownBy(() -> scheduleService.saveShiftSchedules(USER_DETAILS, YEAR, MONTH, EXCEL_FILE))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("저장 실패");

        then(accountRepository).should().existsById(eq(USER_DETAILS.id()));
        then(accountRepository).should().getReferenceById(eq(USER_DETAILS.id()));
        then(scheduleRepository).should().deleteAllByWorkDateBetweenAndAccount_Id(eq(START_DATE), eq(END_DATE), eq(account.getId()));
        then(shiftTypeRepository).should().findAll();
        then(excelScheduleParser).should().parse(eq(EXCEL_FILE), eq(account.getName()));
        then(scheduleRepository).should().saveAll(anyList());
    }

    @DisplayName("해당 월의 근무 스케줄을 일자별 근무 타입으로 변환하여 반환한다.")
    @Test
    void test_getMonthScheduleData_success() {
        String dayShfitLabel = "C";
        String nightShfitLabel = "A";
        AccountEntity account = accountEntity();
        ShiftTypeEntity dayShift = TestDataGenerator.shiftTypeEntity(dayShfitLabel, LocalTime.of(8, 30), LocalTime.of(18, 0), "#FFAA00");
        ShiftTypeEntity nightShift = TestDataGenerator.shiftTypeEntity(nightShfitLabel, LocalTime.of(5, 30), LocalTime.of(15, 0), "#00AAFF");

        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC)))
                .willReturn(List.of(
                        TestDataGenerator.scheduleEntity(account, LocalDate.of(YEAR, MONTH, 1), dayShift),
                        TestDataGenerator.scheduleEntity(account, LocalDate.of(YEAR, MONTH, 15), nightShift),
                        TestDataGenerator.scheduleEntity(account, LocalDate.of(YEAR, MONTH, 30), dayShift)
                ));

        MonthScheduleData result = scheduleService.getMonthScheduleData(USER_DETAILS, YEAR, MONTH);

        assertThat(result.year()).isEqualTo(YEAR);
        assertThat(result.month()).isEqualTo(MONTH);
        assertThat(result.label()).isEqualTo("2026년 9월");
        assertThat(result.shiftCount()).isEqualTo(3);
        assertThat(result.shifts()).containsExactly(
                Map.entry(1, dayShfitLabel),
                Map.entry(15, nightShfitLabel),
                Map.entry(30, dayShfitLabel)
        );

        then(scheduleRepository).should().findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC));
        verifyNoInteractions(accountRepository, excelScheduleParser, shiftTypeRepository);
    }

    @DisplayName("해당 월의 근무 스케줄이 없으면 빈 스케줄 데이터를 반환한다.")
    @Test
    void test_getMonthScheduleData_no_data() {
        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC)))
                .willReturn(List.of());

        MonthScheduleData result = scheduleService.getMonthScheduleData(USER_DETAILS, YEAR, MONTH);

        assertThat(result.year()).isEqualTo(YEAR);
        assertThat(result.month()).isEqualTo(MONTH);
        assertThat(result.label()).isEqualTo("2026년 9월");
        assertThat(result.shiftCount()).isZero();
        assertThat(result.shifts()).isEmpty();

        then(scheduleRepository).should().findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC));
        verifyNoInteractions(accountRepository, excelScheduleParser, shiftTypeRepository);
    }

    @DisplayName("조회 기간은 해당 월의 1일부터 말일까지로 설정된다.")
    @Test
    void test_getMonthScheduleData_end_of_month() {
        LocalDate februaryStart = LocalDate.of(2028, 2, 1);
        LocalDate februaryEnd = LocalDate.of(2028, 2, 29); // 윤년

        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(februaryStart), eq(februaryEnd), eq(WORK_DATE_ASC)))
                .willReturn(List.of());

        MonthScheduleData result = scheduleService.getMonthScheduleData(USER_DETAILS, 2028, 2);

        assertThat(result.label()).isEqualTo("2028년 2월");
        then(scheduleRepository).should().findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(februaryStart), eq(februaryEnd), eq(WORK_DATE_ASC));
        verifyNoInteractions(accountRepository, excelScheduleParser, shiftTypeRepository);
    }

    @DisplayName("근무 타입 목록을 라벨 순으로 정렬된 라벨-색상 Map으로 반환한다.")
    @Test
    void test_getShiftTypes_success() {
        ShiftTypeEntity dayShift = TestDataGenerator.shiftTypeEntity("C", LocalTime.of(8, 30), LocalTime.of(18, 0), "#FFAA00");
        ShiftTypeEntity nightShift = TestDataGenerator.shiftTypeEntity("A", LocalTime.of(5, 30), LocalTime.of(15, 0), "#00AAFF");
        ShiftTypeEntity middleShift = TestDataGenerator.shiftTypeEntity("B", LocalTime.of(7, 0), LocalTime.of(16, 30), "#AAFF00");

        given(shiftTypeRepository.findAll()).willReturn(List.of(dayShift, nightShift, middleShift));

        Map<String, String> result = scheduleService.getShiftTypes();

        assertThat(result).containsExactly(
                Map.entry(nightShift.getLabel(), nightShift.getColor()),
                Map.entry(middleShift.getLabel(), middleShift.getColor()),
                Map.entry(dayShift.getLabel(), dayShift.getColor())
        );

        then(shiftTypeRepository).should().findAll();
        verifyNoInteractions(accountRepository, excelScheduleParser, scheduleRepository);
    }

    @DisplayName("근무 타입 정보가 없으면 빈 Map을 반환한다.")
    @Test
    void test_getShiftTypes_no_data() {
        given(shiftTypeRepository.findAll()).willReturn(List.of());

        Map<String, String> result = scheduleService.getShiftTypes();

        assertThat(result).isEmpty();

        then(shiftTypeRepository).should().findAll();
        verifyNoInteractions(accountRepository, excelScheduleParser, scheduleRepository);
    }

    private AccountEntity accountEntity() {
        return TestDataGenerator.accountEntity(USER_DETAILS.id(), USER_DETAILS.username(), USER_DETAILS.name(), "encoded-password");
    }
}
