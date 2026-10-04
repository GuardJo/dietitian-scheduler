package org.github.guardjo.dientitian.scheduler.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.controller.docs.ScheduleApiDocs;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.MonthScheduleData;
import org.github.guardjo.dientitian.scheduler.api.model.dto.ShiftScheduleUploadRequest;
import org.github.guardjo.dientitian.scheduler.api.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
@Slf4j
@RequiredArgsConstructor
public class ScheduleController implements ScheduleApiDocs {
    private final ScheduleService scheduleService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public BaseResponse<String> uploadSchedule(@AuthenticationPrincipal AccountUserDetails userDetails, @ModelAttribute ShiftScheduleUploadRequest request) {
        log.info("POST : /api/schedules, userId = {}, year = {}, month = {}, excelSize = {}", userDetails.id(), request.year(), request.month(), request.file().getSize());

        scheduleService.saveShiftSchedules(userDetails, request.year(), request.month(), request.file());

        return BaseResponse.of(HttpStatus.CREATED, "Success");
    }

    @GetMapping
    @Override
    public BaseResponse<MonthScheduleData> getSchedule(@AuthenticationPrincipal AccountUserDetails userDetails, @RequestParam int year, @RequestParam int month) {
        log.info("GET : /api/schedules, userId = {}, year = {}, month = {}", userDetails.id(), year, month);

        MonthScheduleData data = scheduleService.getMonthScheduleData(userDetails, year, month);

        return BaseResponse.of(HttpStatus.OK, data);
    }
}
