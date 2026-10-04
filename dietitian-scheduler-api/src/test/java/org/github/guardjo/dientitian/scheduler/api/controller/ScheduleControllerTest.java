package org.github.guardjo.dientitian.scheduler.api.controller;

import org.github.guardjo.dientitian.scheduler.api.exception.ExcelFileReadException;
import org.github.guardjo.dientitian.scheduler.api.jwt.JwtUtil;
import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.github.guardjo.dientitian.scheduler.api.model.dto.MonthScheduleData;
import org.github.guardjo.dientitian.scheduler.api.repository.AccountEntityRepository;
import org.github.guardjo.dientitian.scheduler.api.service.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        mvc.perform(multipart(SCHEDULE_URL)
                        .file(file)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .with(authentication(authenticated()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

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

    @DisplayName("GET: /api/schedules -> 정상 응답")
    @Test
    void test_get_schedule() throws Exception {
        int year = 2026;
        int month = 9;
        Map<Integer, String> shifts = new TreeMap<>(Map.of(4, "a", 5, "c", 6, "b", 10, "a"));
        MonthScheduleData data = new MonthScheduleData(year, month, "2026년 9월", shifts.size(), shifts);
        BaseResponse<MonthScheduleData> expected = BaseResponse.of(HttpStatus.OK, data);
        given(scheduleService.getMonthScheduleData(eq(USER_DETAILS), eq(year), eq(month))).willReturn(data);

        MvcResult result = mvc.perform(get(SCHEDULE_URL)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .with(authentication(authenticated())))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertThat(toMonthScheduleResponse(result)).isEqualTo(expected);

        then(scheduleService).should().getMonthScheduleData(eq(USER_DETAILS), eq(year), eq(month));
    }

    @DisplayName("GET: /api/schedules -> 해당 월의 스케줄이 없을 때")
    @Test
    void test_get_schedule_empty() throws Exception {
        int year = 2026;
        int month = 10;
        MonthScheduleData data = new MonthScheduleData(year, month, "2026년 10월", 0, Map.of());
        given(scheduleService.getMonthScheduleData(eq(USER_DETAILS), eq(year), eq(month))).willReturn(data);

        MvcResult result = mvc.perform(get(SCHEDULE_URL)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .with(authentication(authenticated())))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        BaseResponse<MonthScheduleData> actual = toMonthScheduleResponse(result);
        assertThat(actual.status()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.data().shiftCount()).isZero();
        assertThat(actual.data().shifts()).isEmpty();

        then(scheduleService).should().getMonthScheduleData(eq(USER_DETAILS), eq(year), eq(month));
    }

    @DisplayName("GET: /api/schedules -> 연도/월 범위가 올바르지 않을 때")
    @ParameterizedTest(name = "year = {0}, month = {1}")
    @CsvSource({
            "1999, 9",
            "2026, 0",
            "2026, 13"
    })
    void test_get_schedule_invalid_range(String year, String month) throws Exception {
        mvc.perform(get(SCHEDULE_URL)
                        .param("year", year)
                        .param("month", month)
                        .with(authentication(authenticated())))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @DisplayName("GET: /api/schedules -> 연도가 누락되었을 때")
    @Test
    void test_get_schedule_missing_year() throws Exception {
        mvc.perform(get(SCHEDULE_URL)
                        .param("month", "9")
                        .with(authentication(authenticated())))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @DisplayName("GET: /api/schedules -> 월이 누락되었을 때")
    @Test
    void test_get_schedule_missing_month() throws Exception {
        mvc.perform(get(SCHEDULE_URL)
                        .param("year", "2026")
                        .with(authentication(authenticated())))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @DisplayName("GET: /api/schedules -> 월이 숫자가 아닐 때")
    @Test
    void test_get_schedule_month_type_mismatch() throws Exception {
        mvc.perform(get(SCHEDULE_URL)
                        .param("year", "2026")
                        .param("month", "september")
                        .with(authentication(authenticated())))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
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

    private BaseResponse<MonthScheduleData> toMonthScheduleResponse(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(content, new TypeReference<BaseResponse<MonthScheduleData>>() {
        });
    }
}
