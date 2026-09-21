package org.github.guardjo.dientitian.scheduler.api.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.ShiftScheduleUploadRequest;

@Tag(name = "근무 스케줄 API")
public interface ScheduleApiDocs {
    @Operation(summary = "월별 근무 스케줄 등록", description = "연도/월과 엑셀 파일을 업로드하여 월별 근무 스케줄을 등록한다.")
    BaseResponse<String> uploadSchedule(@Parameter(hidden = true) AccountUserDetails userDetails, @Valid ShiftScheduleUploadRequest request);
}
