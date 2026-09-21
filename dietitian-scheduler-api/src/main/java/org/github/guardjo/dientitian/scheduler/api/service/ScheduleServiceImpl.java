package org.github.guardjo.dientitian.scheduler.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {
    @Override
    public void saveShiftSchedules(Long userId, int year, int month, MultipartFile shiftScheduleFile) {
        log.info("Save shift schedule, userId: {}, year: {}, month: {}", userId, year, month);

        // TODO 기능 구현

        log.info("Shift schedule saved.");
    }
}
