package org.github.guardjo.dientitian.scheduler.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final AccountEntityRepository accountRepository;
    private final ExcelScheduleParser excelScheduleParser;
    private final ShiftTypeEntityRepository shiftTypeRepository;
    private final ScheduleEntityRepository scheduleRepository;

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

    @Override
    public MonthScheduleData getMonthScheduleData(AccountUserDetails userDetails, int year, int month) {
        log.info("Get month schedule, username: {}, year: {}, month: {}", userDetails.getUsername(), year, month);

        Long accountId = userDetails.id();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<ScheduleEntity> schedules = scheduleRepository.findAllByAccount_IdAndWorkDateBetween(accountId, startDate, endDate, Sort.by(Sort.Direction.ASC, "workDate"));

        Map<Integer, String> shiftsData = makeShiftsData(schedules);

        log.info("Month schedule data retrieved.");

        return new MonthScheduleData(year, month, String.format("%d년 %d월", year, month), shiftsData.size(), shiftsData);
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
}
