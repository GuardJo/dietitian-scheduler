package org.github.guardjo.dientitian.scheduler.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.github.guardjo.dientitian.scheduler.api.validation.ExcelFile;
import org.springframework.web.multipart.MultipartFile;

public record ShiftScheduleUploadRequest(
        @Schema(description = "연도", example = "2026")
        @NotNull(message = "연도는 필수입니다.")
        Integer year,

        @Schema(description = "월", example = "9")
        @NotNull(message = "월은 필수입니다.")
        @Min(value = 1, message = "월은 1 이상이어야 합니다.")
        @Max(value = 12, message = "월은 12 이하여야 합니다.")
        Integer month,

        @Schema(description = "업로드할 엑셀 파일", type = "string", format = "binary")
        @NotNull(message = "파일은 필수입니다.")
        @ExcelFile
        MultipartFile file
) {
}
