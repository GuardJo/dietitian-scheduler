package org.github.guardjo.dientitian.scheduler.api.controller.docs;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.LoginRequest;

@Tag(name = "인증 API")
public interface AuthApiDocs {
    @Schema(name = "로그인 요청", description = "주어진 요청 정보를 기반으로 인증 정보를 쿠키에 담아 반환한다.")
    BaseResponse<String> login(@Valid LoginRequest loginRequest, @Parameter(hidden = true) HttpServletResponse response);
}
