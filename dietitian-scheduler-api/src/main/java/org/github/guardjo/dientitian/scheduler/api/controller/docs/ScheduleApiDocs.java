package org.github.guardjo.dientitian.scheduler.api.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.MonthScheduleData;
import org.github.guardjo.dientitian.scheduler.api.model.dto.PersonalScheduleRequest;
import org.github.guardjo.dientitian.scheduler.api.model.dto.ShiftColorUpdateRequest;
import org.github.guardjo.dientitian.scheduler.api.model.dto.ShiftScheduleUploadRequest;

import java.util.Map;

@Tag(name = "근무 스케줄 API")
public interface ScheduleApiDocs {
    @Operation(summary = "월별 근무 스케줄 등록", description = "연도/월과 엑셀 파일을 업로드하여 월별 근무 스케줄을 등록한다.")
    void uploadSchedule(@Parameter(hidden = true) AccountUserDetails userDetails, @Valid ShiftScheduleUploadRequest request);

    @Operation(summary = "월결 근무 스케줄 조회", description = "연도/월에 해당하는 월별 근무 스케줄을 조회한다.")
    BaseResponse<MonthScheduleData> getSchedule(@Parameter(hidden = true) AccountUserDetails userDetails,
                                                @Min(value = 2000, message = "2000년도 이후로 요청해주세요.") @Schema(description = "연도", pattern = "[0-9]{4}", example = "2026") int year,
                                                @Min(value = 1, message = "1 이상의 월을 입력해주세요.") @Max(value = 12, message = "12 이하의 월을 입력해주세요.") @Schema(description = "월", pattern = "[0-9]{1,2}", example = "9") int month);

    @Operation(summary = "근무 스케줄 타입 목록 조회", description = "영양사 근무 스케줄 타입 및 지정 색상 정보를 조회한다.")
    BaseResponse<Map<String, String>> getScheduleTypes();

    @Operation(summary = "근무 타입 색상 변경", description = "특정 근무 타입에 대한 색상을 주어진 필드 내 색상으로 변경한다.")
    void updateShiftColors(@Valid ShiftColorUpdateRequest updateRequest);

    @Operation(summary = "개인 스케줄 등록", description = "일정 시작 및 종료 일자 및 시각을 기준으로 개인 스케줄을 등록한다.")
    void createPersonalSchedule(@Parameter(hidden = true) AccountUserDetails userDetails, @Valid PersonalScheduleRequest request);
}
