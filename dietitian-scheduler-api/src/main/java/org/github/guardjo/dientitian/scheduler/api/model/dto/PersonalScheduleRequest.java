package org.github.guardjo.dientitian.scheduler.api.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalTime;

public record PersonalScheduleRequest(
        @Schema(description = "시작일자", example = "2026-09-01", pattern = "yyyy-MM-dd")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "시작일자를 입력해주세요.")
        LocalDate startDate,

        @Schema(description = "시작시각", example = "09:00", pattern = "HH:mm", nullable = true)
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @Schema(description = "종료일자", example = "2026-09-02", pattern = "yyyy-MM-dd")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "종료일자를 입력해주세요.")
        LocalDate endDate,

        @Schema(description = "종료시각", example = "17:00", pattern = "HH:mm", nullable = true)
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        @Schema(description = "하루종일 여부", example = "true", defaultValue = "false")
        boolean allDay,

        @Schema(description = "일정명", example = "개인일정1")
        @NotEmpty(message = "제목을 입력해주세요.")
        @Length(max = 60, min = 1, message = "제목은 60자 이내로 작성해주세요.")
        String title,

        @Schema(description = "비고", example = "메모", nullable = true)
        String memo
) {
}
