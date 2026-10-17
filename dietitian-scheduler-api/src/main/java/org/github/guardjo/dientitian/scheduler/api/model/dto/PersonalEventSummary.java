package org.github.guardjo.dientitian.scheduler.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record PersonalEventSummary(
        @Schema(description = "개인 일정 식별키", example = "1")
        Long id,

        @Schema(description = "개인 일정 제목", example = "여행")
        String title,

        @Schema(description = "개인 일정 시작 시각", example = "09:00", pattern = "HH:mm", nullable = true)
        LocalTime startTime,

        @Schema(description = "개인 일정 종료 시각", example = "17:00", pattern = "HH:mm", nullable = true)
        LocalTime endTime
) {
}
