package org.github.guardjo.dientitian.scheduler.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.controller.docs.ScheduleApiDocs;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.ShiftScheduleUploadRequest;
import org.github.guardjo.dientitian.scheduler.api.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
