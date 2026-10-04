package org.github.guardjo.dientitian.scheduler.api.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.LoginRequest;

@Tag(name = "인증 API")
public interface AuthApiDocs {
    @Operation(summary = "로그인 요청", description = "주어진 요청 정보를 기반으로 인증 정보를 쿠키에 담아 반환한다.")
    BaseResponse<String> login(@Valid LoginRequest loginRequest, @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "로그아웃 요청", description = "요청 사용자의 인증/인가 정보를 초기화한ㄷ.")
    void logout(@Parameter(hidden = true) AccountUserDetails userDetails, @Parameter(hidden = true) HttpServletResponse response);
}
