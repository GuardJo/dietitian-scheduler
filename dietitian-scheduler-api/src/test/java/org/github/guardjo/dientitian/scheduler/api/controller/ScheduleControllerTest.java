package org.github.guardjo.dientitian.scheduler.api.controller;

import org.github.guardjo.dientitian.scheduler.api.exception.ExcelFileReadException;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.service.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {
    private static final String SCHEDULE_URL = "/api/schedules";
    private static final AccountUserDetails USER_DETAILS = new AccountUserDetails(1L, "tester", "테스터", "password");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService scheduleService;

    // JwtAuthenticationFilter 빈 생성에 필요한 의존성
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AccountEntityRepository accountEntityRepository;

    @DisplayName("POST: /api/schedules -> 정상 응답")
    @Test
    void test_upload_schedule() throws Exception {
        int year = 2026;
        int month = 9;
        MockMultipartFile file = excelFile();
        BaseResponse<String> expected = BaseResponse.of(HttpStatus.CREATED, "Success");

        MvcResult result = mvc.perform(multipart(SCHEDULE_URL)
                        .file(file)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .with(authentication(authenticated()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertThat(toBaseResponse(result)).isEqualTo(expected);

        then(scheduleService).should().saveShiftSchedules(eq(USER_DETAILS), eq(year), eq(month), eq(file));
    }

    @DisplayName("POST: /api/schedules -> 월 범위가 올바르지 않을 때")
    @Test
    void test_upload_schedule_invalid_month() throws Exception {
        mvc.perform(multipart(SCHEDULE_URL)
                        .file(excelFile())
                        .param("year", "2026")
                        .param("month", "13")
                        .with(authentication(authenticated()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @DisplayName("POST: /api/schedules -> 연도가 누락되었을 때")
    @Test
    void test_upload_schedule_missing_year() throws Exception {
        mvc.perform(multipart(SCHEDULE_URL)
                        .file(excelFile())
                        .param("month", "9")
                        .with(authentication(authenticated()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @DisplayName("POST: /api/schedules -> 파일이 누락되었을 때")
    @Test
    void test_upload_schedule_missing_file() throws Exception {
        mvc.perform(multipart(SCHEDULE_URL)
                        .param("year", "2026")
                        .param("month", "9")
                        .with(authentication(authenticated()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @DisplayName("POST: /api/schedules -> 엑셀 파일이 아닐 때")
    @Test
    void test_upload_schedule_not_excel_file() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile("file", "schedule.txt", "text/plain", new byte[]{1, 2, 3});

        mvc.perform(multipart(SCHEDULE_URL)
                        .file(textFile)
                        .param("year", "2026")
                        .param("month", "9")
                        .with(authentication(authenticated()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @DisplayName("POST: /api/schedules -> 파일 읽기 작업 실패 시")
    @Test
    void test_upload_schedule_file_read_exception() throws Exception {
        int year = 2026;
        int month = 9;
        MockMultipartFile excelFile = excelFile();
        String exceptionMessage = "엑셀 파일 조회 실패";
        willThrow(new ExcelFileReadException(exceptionMessage, new Throwable())).given(scheduleService).saveShiftSchedules(eq(USER_DETAILS), eq(year), eq(month), eq(excelFile));

        MvcResult response = mvc.perform(multipart(SCHEDULE_URL)
                        .file(excelFile)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .with(authentication(authenticated()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn();

        BaseResponse<String> actual = toBaseResponse(response);
        assertThat(actual.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(actual.data()).isEqualTo(exceptionMessage);
    }

    private Authentication authenticated() {
        return new UsernamePasswordAuthenticationToken(USER_DETAILS, null, USER_DETAILS.getAuthorities());
    }

    private MockMultipartFile excelFile() {
        return new MockMultipartFile("file", "schedule.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1, 2, 3});
    }

    private BaseResponse<String> toBaseResponse(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(content, new TypeReference<BaseResponse<String>>() {
        });
    }
}
