package org.github.guardjo.dientitian.scheduler.api.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    // DB time 컬럼 정밀도로 인한 반올림(24:00:00)을 피하기 위해 초 단위까지만 사용
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    private final AccountEntityRepository accountRepository;
    private final ExcelScheduleParser excelScheduleParser;
    private final ShiftTypeEntityRepository shiftTypeRepository;
    private final ScheduleEntityRepository scheduleRepository;
    private final PersonalEventEntityRepository personalEventRepository;

    @Transactional
    @Override
    public void saveShiftSchedules(AccountUserDetails userDetails, int year, int month, MultipartFile shiftScheduleFile) {
        log.info("Save shift schedule, username: {}, year: {}, month: {}", userDetails.getUsername(), year, month);

        if (!accountRepository.existsById(userDetails.id())) {
            log.warn("Account not found, userId: {}", userDetails.id());
            throw new UsernameNotFoundException("해당 사용자 정보를 찾을 수 없습니다.");
        }

        AccountEntity account = accountRepository.getReferenceById(userDetails.id());

        // 엑셀 데이터 업로드의 경우 해달 월 데이터가 이미 있을 경우, 초기화 후 저장하도록 한다.
        clearSchedules(account, year, month);

        List<ScheduleEntity> scheduleEntities = parseExcel(shiftScheduleFile, userDetails.name(), account, year, month);
        scheduleRepository.saveAll(scheduleEntities);

        log.info("Shift schedule saved, totalSchedules: {}", scheduleEntities.size());
    }

    @Transactional(readOnly = true)
    @Override
    public MonthScheduleData getMonthScheduleData(AccountUserDetails userDetails, int year, int month) {
        log.info("Get month schedule, username: {}, year: {}, month: {}", userDetails.getUsername(), year, month);

        Long accountId = userDetails.id();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<ScheduleEntity> schedules = scheduleRepository.findAllByAccount_IdAndWorkDateBetween(accountId, startDate, endDate, Sort.by(Sort.Direction.ASC, "workDate"));
        Map<Integer, String> shiftsData = makeShiftsData(schedules);

        List<PersonalEventEntity> personalEvents = personalEventRepository.findAllByAccountIdAndEventDateBetween(accountId, startDate, endDate,
                Sort.by(Sort.Order.asc("eventDate"), Sort.Order.asc("startTime")));
        Map<Integer, List<PersonalEventSummary>> personalEventSummaries = makePersonalEventSummaryMap(personalEvents);

        log.info("Month schedule data retrieved.");

        return new MonthScheduleData(year, month, String.format("%d년 %d월", year, month), shiftsData.size(), shiftsData, personalEventSummaries);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, String> getShiftTypes() {
        log.info("Get shift types");

        List<ShiftTypeEntity> shiftTypes = shiftTypeRepository.findAll();

        Map<String, String> shiftTypeMap = shiftTypes.stream()
                .collect(HashMap::new, (map, shiftType) -> {
                    map.put(shiftType.getLabel(), shiftType.getColor());
                }, Map::putAll);

        log.info("Shift types retrieved. totalTypes = {}", shiftTypeMap.size());
        return shiftTypeMap;
    }

    @Transactional
    @Override
    public void updateShiftColors(String label, String color) {
        log.info("Update shift color, label: {}, color: {}", label, color);

        ShiftTypeEntity shiftTypeEntity = shiftTypeRepository.findByLabel(label)
                .orElseThrow(() -> new EntityNotFoundException("근무타입 정보를 찾을 수 없습니다."));
        String lastColor = shiftTypeEntity.getColor();
        shiftTypeEntity.setColor(color);

        log.info("Shift color updated, label: {}, color: {} -> {}", label, lastColor, color);
    }

    @Transactional
    @Override
    public void createPersonalSchedule(Long userId, PersonalScheduleRequest request) {
        log.info("Create personal schedule, userId: {}, title: {}", userId, request.title());

        AccountEntity accountEntity = accountRepository.getReferenceById(userId);
        List<PersonalEventEntity> personalEventEntities = createPersonalEvents(accountEntity, request);
        personalEventRepository.saveAll(personalEventEntities);

        log.info("Personal schedule created, insertedCount: {}", personalEventEntities.size());
    }

    private List<ScheduleEntity> parseExcel(MultipartFile excelFile, String name, AccountEntity account, int year, int month) {
        List<ShiftTypeEntity> shiftTypes = shiftTypeRepository.findAll();
        List<DailyShift> dailyShifts = excelScheduleParser.parse(excelFile, name);

        return dailyShifts.stream()
                .map(dailyShift -> ScheduleEntity.builder()
                        .shiftType(getShiftType(shiftTypes, dailyShift.shiftCode()))
                        .account(account)
                        .workDate(LocalDate.of(year, month, dailyShift.day()))
                        .build())
                .filter(scheduleEntity -> Objects.nonNull(scheduleEntity.getShiftType()))
                .toList();
    }

    private ShiftTypeEntity getShiftType(List<ShiftTypeEntity> shiftTypes, String shiftType) {
        return shiftTypes.stream()
                .filter(type -> type.getLabel().equals(shiftType))
                .findFirst().orElse(null);
    }

    private void clearSchedules(AccountEntity account, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        log.info("Clear schedules, account: {}, startDate: {}, endDate: {}", account.getUsername(), startDate, endDate);
        int clearedCount = scheduleRepository.deleteAllByWorkDateBetweenAndAccount_Id(startDate, endDate, account.getId());
        log.info("Schedules cleared, clearedCount: {}", clearedCount);
    }

    /*
    ScheduleEntity 목록 -> 일자별 근무 타입 Map 변환
     */
    private Map<Integer, String> makeShiftsData(List<ScheduleEntity> schedules) {
        return schedules.stream()
                .collect(TreeMap::new, (map, schedule) -> {
                    map.put(schedule.getWorkDate().getDayOfMonth(), schedule.getShiftType().getLabel());
                }, Map::putAll);
    }

    /*
    요청 데이터 기반 PersonalEvent Entity 목록 생성
     */
    private List<PersonalEventEntity> createPersonalEvents(AccountEntity accountEntity, PersonalScheduleRequest request) {
        // 시작일자부터 종료일자까지(종료일자 포함) 일자별 스케줄 생성
        List<PersonalEventEntity> personalEventEntities = new ArrayList<>();
        LocalDate baseDate = request.startDate();
        while (!baseDate.isAfter(request.endDate())) {
            boolean isFirstDay = baseDate.isEqual(request.startDate());
            boolean isLastDay = baseDate.isEqual(request.endDate());

            PersonalEventEntity personalEventEntity = PersonalEventEntity.builder()
                    .account(accountEntity)
                    .title(request.title())
                    .memo(request.memo())
                    .eventDate(baseDate)
                    .startTime(isFirstDay && !request.allDay() ? request.startTime() : LocalTime.MIN)
                    .endTime(isLastDay && !request.allDay() ? request.endTime() : END_OF_DAY)
                    .build();
            personalEventEntities.add(personalEventEntity);
            baseDate = baseDate.plusDays(1);
        }

        return personalEventEntities;
    }

    /*
    PersonalEvent Entity 목록을 일자별 목록으로 mapping (일자 내 순서는 조회 순서 유지)
     */
    private Map<Integer, List<PersonalEventSummary>> makePersonalEventSummaryMap(List<PersonalEventEntity> personalEventEntities) {
        return personalEventEntities.stream()
                .collect(Collectors.groupingBy(personalEvent -> personalEvent.getEventDate().getDayOfMonth(), TreeMap::new,
                        Collectors.mapping(this::makePersonalEventSummary, Collectors.toList())));
    }

    /*
    PersonalEvent Entity에 대한 summary 객체 구성
     */
    private PersonalEventSummary makePersonalEventSummary(PersonalEventEntity personalEventEntity) {
        return new PersonalEventSummary(
                personalEventEntity.getId(),
                personalEventEntity.getTitle(),
                personalEventEntity.getStartTime(),
                personalEventEntity.getEndTime()
        );
    }
}
