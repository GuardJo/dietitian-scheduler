package org.github.guardjo.dientitian.scheduler.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "회원 아이디", example = "tester")
        @NotBlank(message = "아이디는 필수입니다.")
        String username,

        @Schema(description = "회원 비밀번호", example = "password1!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
