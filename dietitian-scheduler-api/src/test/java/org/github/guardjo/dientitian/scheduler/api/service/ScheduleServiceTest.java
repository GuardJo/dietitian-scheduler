package org.github.guardjo.dientitian.scheduler.api.service;

import jakarta.persistence.EntityNotFoundException;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.dto.DailyShift;
import org.github.guardjo.dientitian.scheduler.api.model.dto.MonthScheduleData;
import org.github.guardjo.dientitian.scheduler.api.model.dto.PersonalEventSummary;
import org.github.guardjo.dientitian.scheduler.api.model.dto.PersonalScheduleRequest;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.PersonalEventEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ScheduleEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ShiftTypeEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.repository.PersonalEventEntityRepository;
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
import java.util.Optional;

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
    private static final Sort EVENT_DATE_START_TIME_ASC = Sort.by(Sort.Order.asc("eventDate"), Sort.Order.asc("startTime"));
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    @Mock
    private AccountEntityRepository accountRepository;

    @Mock
    private ExcelScheduleParser excelScheduleParser;

    @Mock
    private ShiftTypeEntityRepository shiftTypeRepository;

    @Mock
    private ScheduleEntityRepository scheduleRepository;

    @Mock
    private PersonalEventEntityRepository personalEventRepository;

    @Captor
    private ArgumentCaptor<List<ScheduleEntity>> scheduleEntitiesCaptor;

    @Captor
    private ArgumentCaptor<List<PersonalEventEntity>> personalEventEntitiesCaptor;

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

    @DisplayName("해당 월의 개인 일정을 일자별 개인 일정 요약 정보로 변환하여 반환한다.")
    @Test
    void test_getMonthScheduleData_personal_events() {
        AccountEntity account = accountEntity();

        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC)))
                .willReturn(List.of());
        given(personalEventRepository.findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(EVENT_DATE_START_TIME_ASC)))
                .willReturn(List.of(
                        TestDataGenerator.personalEventEntity(1L, account, "치과 예약", LocalDate.of(YEAR, MONTH, 3), LocalTime.of(19, 0), LocalTime.of(20, 0)),
                        TestDataGenerator.personalEventEntity(2L, account, "휴가", LocalDate.of(YEAR, MONTH, 10), LocalTime.MIN, END_OF_DAY),
                        TestDataGenerator.personalEventEntity(3L, account, "회식", LocalDate.of(YEAR, MONTH, 30), LocalTime.of(18, 30), END_OF_DAY)
                ));

        MonthScheduleData result = scheduleService.getMonthScheduleData(USER_DETAILS, YEAR, MONTH);

        assertThat(result.personalEvents()).containsExactly(
                Map.entry(3, List.of(new PersonalEventSummary(1L, "치과 예약", LocalTime.of(19, 0), LocalTime.of(20, 0)))),
                Map.entry(10, List.of(new PersonalEventSummary(2L, "휴가", LocalTime.MIN, END_OF_DAY))),
                Map.entry(30, List.of(new PersonalEventSummary(3L, "회식", LocalTime.of(18, 30), END_OF_DAY)))
        );

        then(scheduleRepository).should().findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC));
        then(personalEventRepository).should().findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(EVENT_DATE_START_TIME_ASC));
        verifyNoInteractions(accountRepository, excelScheduleParser, shiftTypeRepository);
    }

    @DisplayName("같은 일자에 개인 일정이 여러 개면 조회 순서대로 모두 반환한다.")
    @Test
    void test_getMonthScheduleData_personal_events_same_day() {
        AccountEntity account = accountEntity();
        LocalDate eventDate = LocalDate.of(YEAR, MONTH, 5);

        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC)))
                .willReturn(List.of());
        // 저장소에서 시작 시각 오름차순으로 정렬되어 반환된다.
        given(personalEventRepository.findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(EVENT_DATE_START_TIME_ASC)))
                .willReturn(List.of(
                        TestDataGenerator.personalEventEntity(1L, account, "오전 일정", eventDate, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                        TestDataGenerator.personalEventEntity(2L, account, "오후 일정", eventDate, LocalTime.of(15, 0), LocalTime.of(16, 0)),
                        TestDataGenerator.personalEventEntity(3L, account, "다른 날 일정", eventDate.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0))
                ));

        MonthScheduleData result = scheduleService.getMonthScheduleData(USER_DETAILS, YEAR, MONTH);

        assertThat(result.personalEvents()).containsExactly(
                Map.entry(5, List.of(
                        new PersonalEventSummary(1L, "오전 일정", LocalTime.of(9, 0), LocalTime.of(10, 0)),
                        new PersonalEventSummary(2L, "오후 일정", LocalTime.of(15, 0), LocalTime.of(16, 0))
                )),
                Map.entry(6, List.of(new PersonalEventSummary(3L, "다른 날 일정", LocalTime.of(9, 0), LocalTime.of(10, 0))))
        );
    }

    @DisplayName("근무 스케줄과 개인 일정을 함께 조회하여 반환한다.")
    @Test
    void test_getMonthScheduleData_shifts_and_personal_events() {
        AccountEntity account = accountEntity();
        ShiftTypeEntity dayShift = TestDataGenerator.shiftTypeEntity("C", LocalTime.of(8, 30), LocalTime.of(18, 0), "#FFAA00");

        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC)))
                .willReturn(List.of(
                        TestDataGenerator.scheduleEntity(account, LocalDate.of(YEAR, MONTH, 1), dayShift),
                        TestDataGenerator.scheduleEntity(account, LocalDate.of(YEAR, MONTH, 2), dayShift)
                ));
        given(personalEventRepository.findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(EVENT_DATE_START_TIME_ASC)))
                .willReturn(List.of(
                        TestDataGenerator.personalEventEntity(1L, account, "치과 예약", LocalDate.of(YEAR, MONTH, 2), LocalTime.of(19, 0), LocalTime.of(20, 0))
                ));

        MonthScheduleData result = scheduleService.getMonthScheduleData(USER_DETAILS, YEAR, MONTH);

        // 개인 일정은 근무 스케줄 수에 포함되지 않는다.
        assertThat(result.shiftCount()).isEqualTo(2);
        assertThat(result.shifts()).containsExactly(Map.entry(1, "C"), Map.entry(2, "C"));
        assertThat(result.personalEvents()).containsExactly(
                Map.entry(2, List.of(new PersonalEventSummary(1L, "치과 예약", LocalTime.of(19, 0), LocalTime.of(20, 0))))
        );
    }

    @DisplayName("해당 월의 개인 일정이 없으면 빈 개인 일정 데이터를 반환한다.")
    @Test
    void test_getMonthScheduleData_no_personal_events() {
        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(WORK_DATE_ASC)))
                .willReturn(List.of());
        given(personalEventRepository.findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(EVENT_DATE_START_TIME_ASC)))
                .willReturn(List.of());

        MonthScheduleData result = scheduleService.getMonthScheduleData(USER_DETAILS, YEAR, MONTH);

        assertThat(result.personalEvents()).isEmpty();
        then(personalEventRepository).should().findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(START_DATE), eq(END_DATE), eq(EVENT_DATE_START_TIME_ASC));
    }

    @DisplayName("개인 일정 조회 기간은 해당 월의 1일부터 말일까지로 설정된다.")
    @Test
    void test_getMonthScheduleData_personal_events_end_of_month() {
        LocalDate februaryStart = LocalDate.of(2028, 2, 1);
        LocalDate februaryEnd = LocalDate.of(2028, 2, 29); // 윤년

        given(scheduleRepository.findAllByAccount_IdAndWorkDateBetween(eq(USER_DETAILS.id()), eq(februaryStart), eq(februaryEnd), eq(WORK_DATE_ASC)))
                .willReturn(List.of());
        given(personalEventRepository.findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(februaryStart), eq(februaryEnd), eq(EVENT_DATE_START_TIME_ASC)))
                .willReturn(List.of());

        scheduleService.getMonthScheduleData(USER_DETAILS, 2028, 2);

        then(personalEventRepository).should().findAllByAccountIdAndEventDateBetween(eq(USER_DETAILS.id()), eq(februaryStart), eq(februaryEnd), eq(EVENT_DATE_START_TIME_ASC));
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

    @DisplayName("근무 타입의 색상을 정상적으로 변경한다.")
    @Test
    void test_updateShiftColors_success() {
        String label = "A";
        String oldColor = "#FF0000";
        String newColor = "#00FF00";
        ShiftTypeEntity shiftType = TestDataGenerator.shiftTypeEntity(label, LocalTime.of(8, 30), LocalTime.of(18, 0), oldColor);

        given(shiftTypeRepository.findByLabel(eq(label))).willReturn(Optional.of(shiftType));

        assertThatCode(() -> scheduleService.updateShiftColors(label, newColor))
                .doesNotThrowAnyException();

        assertThat(shiftType.getColor()).isEqualTo(newColor);
        then(shiftTypeRepository).should().findByLabel(eq(label));
        verifyNoInteractions(accountRepository, excelScheduleParser, scheduleRepository);
    }

    @DisplayName("존재하지 않는 근무 타입의 색상을 변경하려고 하면 예외가 발생한다.")
    @Test
    void test_updateShiftColors_shift_type_not_found() {
        String label = "UNKNOWN";
        String newColor = "#00FF00";

        given(shiftTypeRepository.findByLabel(eq(label))).willReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.updateShiftColors(label, newColor))
                .isInstanceOf(EntityNotFoundException.class);

        then(shiftTypeRepository).should().findByLabel(eq(label));
        verifyNoInteractions(accountRepository, excelScheduleParser, scheduleRepository);
    }

    @DisplayName("시작일자와 종료일자가 같으면 요청 시각 그대로 단일 개인 일정을 저장한다.")
    @Test
    void test_createPersonalSchedule_same_day() {
        AccountEntity account = accountEntity();
        LocalDate eventDate = LocalDate.of(YEAR, MONTH, 1);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        PersonalScheduleRequest request = new PersonalScheduleRequest(
                eventDate, startTime, eventDate, endTime, false, "개인일정1", "메모");

        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);

        assertThatCode(() -> scheduleService.createPersonalSchedule(USER_DETAILS.id(), request))
                .doesNotThrowAnyException();

        then(accountRepository).should().getReferenceById(eq(USER_DETAILS.id()));
        then(personalEventRepository).should().saveAll(personalEventEntitiesCaptor.capture());
        List<PersonalEventEntity> saved = personalEventEntitiesCaptor.getValue();

        assertThat(saved).hasSize(1);
        PersonalEventEntity event = saved.getFirst();
        assertThat(event.getAccount()).isEqualTo(account);
        assertThat(event.getTitle()).isEqualTo(request.title());
        assertThat(event.getMemo()).isEqualTo(request.memo());
        assertThat(event.getEventDate()).isEqualTo(eventDate);
        assertThat(event.getStartTime()).isEqualTo(startTime);
        assertThat(event.getEndTime()).isEqualTo(endTime);
        verifyNoInteractions(excelScheduleParser, shiftTypeRepository, scheduleRepository);
    }

    @DisplayName("하루종일 일정이면 시작 시각은 00:00, 종료 시각은 23:59:59로 저장한다.")
    @Test
    void test_createPersonalSchedule_same_day_all_day() {
        AccountEntity account = accountEntity();
        LocalDate eventDate = LocalDate.of(YEAR, MONTH, 1);
        PersonalScheduleRequest request = new PersonalScheduleRequest(
                eventDate, null, eventDate, null, true, "개인일정1", null);

        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);

        scheduleService.createPersonalSchedule(USER_DETAILS.id(), request);

        then(personalEventRepository).should().saveAll(personalEventEntitiesCaptor.capture());
        List<PersonalEventEntity> saved = personalEventEntitiesCaptor.getValue();

        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getEventDate()).isEqualTo(eventDate);
        assertThat(saved.getFirst().getStartTime()).isEqualTo(LocalTime.MIN);
        assertThat(saved.getFirst().getEndTime()).isEqualTo(END_OF_DAY);
        assertThat(saved.getFirst().getMemo()).isNull();
    }

    @DisplayName("시작일자와 종료일자가 다른 하루종일 일정이면 종료일자를 포함한 모든 일자를 00:00~23:59:59로 저장한다.")
    @Test
    void test_createPersonalSchedule_multiple_days() {
        AccountEntity account = accountEntity();
        LocalDate startDate = LocalDate.of(YEAR, MONTH, 1);
        LocalDate endDate = LocalDate.of(YEAR, MONTH, 3);
        PersonalScheduleRequest request = new PersonalScheduleRequest(
                startDate, null, endDate, null, true, "휴가", "연차");

        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);

        scheduleService.createPersonalSchedule(USER_DETAILS.id(), request);

        then(accountRepository).should().getReferenceById(eq(USER_DETAILS.id()));
        then(personalEventRepository).should().saveAll(personalEventEntitiesCaptor.capture());
        List<PersonalEventEntity> saved = personalEventEntitiesCaptor.getValue();

        assertThat(saved).extracting(PersonalEventEntity::getEventDate)
                .containsExactly(startDate, startDate.plusDays(1), endDate);
        assertThat(saved).extracting(PersonalEventEntity::getAccount).containsOnly(account);
        assertThat(saved).extracting(PersonalEventEntity::getTitle).containsOnly(request.title());
        assertThat(saved).extracting(PersonalEventEntity::getMemo).containsOnly(request.memo());
        assertThat(saved).extracting(PersonalEventEntity::getStartTime).containsOnly(LocalTime.MIN);
        assertThat(saved).extracting(PersonalEventEntity::getEndTime).containsOnly(END_OF_DAY);
    }

    @DisplayName("시작일자와 종료일자가 다른 시간 지정 일정이면 첫날은 시작시각~23:59:59, 중간 날은 하루종일, 마지막 날은 00:00~종료시각으로 저장한다.")
    @Test
    void test_createPersonalSchedule_multiple_days_with_time() {
        AccountEntity account = accountEntity();
        LocalDate startDate = LocalDate.of(YEAR, MONTH, 1);
        LocalDate endDate = LocalDate.of(YEAR, MONTH, 3);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        PersonalScheduleRequest request = new PersonalScheduleRequest(
                startDate, startTime, endDate, endTime, false, "출장", null);

        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);

        scheduleService.createPersonalSchedule(USER_DETAILS.id(), request);

        then(personalEventRepository).should().saveAll(personalEventEntitiesCaptor.capture());
        List<PersonalEventEntity> saved = personalEventEntitiesCaptor.getValue();

        assertThat(saved).extracting(PersonalEventEntity::getEventDate, PersonalEventEntity::getStartTime, PersonalEventEntity::getEndTime)
                .containsExactly(
                        tuple(startDate, startTime, END_OF_DAY),
                        tuple(startDate.plusDays(1), LocalTime.MIN, END_OF_DAY),
                        tuple(endDate, LocalTime.MIN, endTime)
                );
    }

    @DisplayName("시작일자와 종료일자가 하루 차이인 시간 지정 일정이면 중간 날 없이 첫날과 마지막 날만 저장한다.")
    @Test
    void test_createPersonalSchedule_two_days_with_time() {
        AccountEntity account = accountEntity();
        LocalDate startDate = LocalDate.of(YEAR, MONTH, 1);
        LocalDate endDate = startDate.plusDays(1);
        LocalTime startTime = LocalTime.of(22, 0);
        LocalTime endTime = LocalTime.of(6, 0);
        PersonalScheduleRequest request = new PersonalScheduleRequest(
                startDate, startTime, endDate, endTime, false, "야간 일정", null);

        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);

        scheduleService.createPersonalSchedule(USER_DETAILS.id(), request);

        then(personalEventRepository).should().saveAll(personalEventEntitiesCaptor.capture());
        List<PersonalEventEntity> saved = personalEventEntitiesCaptor.getValue();

        assertThat(saved).extracting(PersonalEventEntity::getEventDate, PersonalEventEntity::getStartTime, PersonalEventEntity::getEndTime)
                .containsExactly(
                        tuple(startDate, startTime, END_OF_DAY),
                        tuple(endDate, LocalTime.MIN, endTime)
                );
    }

    @DisplayName("개인 일정을 저장하는 중 오류가 발생하면 예외가 발생한다.")
    @Test
    void test_createPersonalSchedule_save_error() {
        AccountEntity account = accountEntity();
        LocalDate eventDate = LocalDate.of(YEAR, MONTH, 1);
        PersonalScheduleRequest request = new PersonalScheduleRequest(
                eventDate, null, eventDate, null, true, "개인일정1", null);

        given(accountRepository.getReferenceById(eq(USER_DETAILS.id()))).willReturn(account);
        given(personalEventRepository.saveAll(anyList())).willThrow(new DataIntegrityViolationException("저장 실패"));

        assertThatThrownBy(() -> scheduleService.createPersonalSchedule(USER_DETAILS.id(), request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("저장 실패");

        then(accountRepository).should().getReferenceById(eq(USER_DETAILS.id()));
        then(personalEventRepository).should().saveAll(anyList());
    }

    private AccountEntity accountEntity() {
        return TestDataGenerator.accountEntity(USER_DETAILS.id(), USER_DETAILS.username(), USER_DETAILS.name(), "encoded-password");
    }
}
