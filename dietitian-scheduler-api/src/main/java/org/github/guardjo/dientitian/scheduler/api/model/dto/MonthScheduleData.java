package org.github.guardjo.dientitian.scheduler.api.model.dto;

import java.util.Map;

public record MonthScheduleData(
        int year,
        int month,
        String label,
        int shiftCount,
        Map<Integer, String> shifts
) {
}
