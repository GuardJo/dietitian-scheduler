package org.github.guardjo.dientitian.scheduler.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record ShiftColorUpdateRequest(
        @Schema(description = "근무타입", example = "ALL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "근무타입 값이 비었습니다.")
        String shiftType,

        @Schema(description = "색상", example = "#FF0000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "색상 정보를 입력해주세요.")
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "올바른 색상 형식이 아닙니다. (예: #FF0000)")
        String color
) {
}
