package org.github.guardjo.dientitian.scheduler.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.dto.DailyShift;
import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ScheduleEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ShiftTypeEntity;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.repository.ScheduleEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.repository.ShiftTypeEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.util.ExcelScheduleParser;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final AccountEntityRepository accountRepository;
    private final ExcelScheduleParser excelScheduleParser;
    private final ShiftTypeEntityRepository shiftTypeRepository;
    private final ScheduleEntityRepository scheduleRepository;

    @Override
    public void saveShiftSchedules(AccountUserDetails userDetails, int year, int month, MultipartFile shiftScheduleFile) {
        log.info("Save shift schedule, username: {}, year: {}, month: {}", userDetails.getUsername(), year, month);

        if (!accountRepository.existsById(userDetails.id())) {
            log.warn("Account not found, userId: {}", userDetails.id());
            throw new UsernameNotFoundException("해당 사용자 정보를 찾을 수 없습니다.");
        }

        AccountEntity account = accountRepository.getReferenceById(userDetails.id());

        List<ScheduleEntity> scheduleEntities = parseExcel(shiftScheduleFile, userDetails.name(), account, year, month);
        scheduleRepository.saveAll(scheduleEntities);

        log.info("Shift schedule saved, totalSchedules: {}", scheduleEntities.size());
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
}
