package org.github.guardjo.dientitian.scheduler.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record MonthScheduleData(
        @Schema(description = "연도", pattern = "[0-9]{4}", example = "2026")
        int year,

        @Schema(description = "월", pattern = "[0-9]{1,2}", example = "9")
        int month,

        @Schema(description = "스케줄 연도 및 월 정보", example = "2026년 9월")
        String label,

        @Schema(description = "스케줄 수", example = "3")
        int shiftCount,

        @Schema(description = "일자별 근무 타입", example = "{'1': 'C', '2': 'A', '3': 'C'}")
        Map<Integer, String> shifts
) {
}
