package org.github.guardjo.dientitian.scheduler.api.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.github.guardjo.dientitian.scheduler.api.model.dto.DailyShift;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ExcelScheduleParserTest {
    // ExcelScheduleParser의 private 상수와 동일한 값 (13행 헤더 / 15행부터 데이터 / D열 성명 / E열부터 스케줄)
    private static final int HEADER_ROW = 12;
    private static final int SCHEDULE_START_ROW = 14;
    private static final int NAME_COL = 3;
    private static final int SCHEDULE_START_COL = 4;

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExcelScheduleParser excelScheduleParser = new ExcelScheduleParser();

    @DisplayName("성명이 일치하는 행의 근무 스케줄을 일자 순서대로 추출한다.")
    @Test
    void test_parse_success() throws IOException {
        MockMultipartFile file = excelFile(
                List.of(1, 2, 3),
                new LinkedHashMap<>(Map.of("김아무개", List.of("C", "OFF", "A")))
        );

        List<DailyShift> actual = excelScheduleParser.parse(file, "김아무개");

        assertThat(actual).containsExactly(
                new DailyShift(1, "C"),
                new DailyShift(2, "OFF"),
                new DailyShift(3, "A")
        );
    }

    @DisplayName("여러 인원의 스케줄 중 성명이 일치하는 행만 추출한다.")
    @Test
    void test_parse_with_multiple_people() throws IOException {
        Map<String, List<String>> people = new LinkedHashMap<>();
        people.put("김아무개", List.of("C", "C", "OFF"));
        people.put("박아무개", List.of("A", "B", "OFF"));
        MockMultipartFile file = excelFile(List.of(1, 2, 3), people);

        List<DailyShift> actual = excelScheduleParser.parse(file, "박아무개");

        assertThat(actual).containsExactly(
                new DailyShift(1, "A"),
                new DailyShift(2, "B"),
                new DailyShift(3, "OFF")
        );
    }

    @DisplayName("데이터 행 사이에 빈 행이 있어도 건너뛰고 대상 성명을 찾는다.")
    @Test
    void test_parse_skips_blank_rows() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            writeHeaderRow(sheet, List.of(1, 2));
            // SCHEDULE_START_ROW, SCHEDULE_START_ROW + 1행은 생성하지 않아 빈 행(null)으로 남김
            writeDataRow(sheet, SCHEDULE_START_ROW + 2, "최아무개", List.of("A", "B"));

            MockMultipartFile file = toMultipartFile(workbook);

            List<DailyShift> actual = excelScheduleParser.parse(file, "최아무개");

            assertThat(actual).containsExactly(
                    new DailyShift(1, "A"),
                    new DailyShift(2, "B")
            );
        }
    }

    @DisplayName("숫자가 아닌 헤더 셀과 문자열이 아닌 스케줄 셀은 결과에서 제외한다.")
    @Test
    void test_parse_skips_non_matching_cell_types() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row headerRow = sheet.createRow(HEADER_ROW);
            headerRow.createCell(SCHEDULE_START_COL).setCellValue(1);
            headerRow.createCell(SCHEDULE_START_COL + 1).setCellValue("비고"); // 숫자가 아닌 헤더 셀
            headerRow.createCell(SCHEDULE_START_COL + 2).setCellValue(3);

            Row dataRow = sheet.createRow(SCHEDULE_START_ROW);
            dataRow.createCell(NAME_COL).setCellValue("손아무개");
            dataRow.createCell(SCHEDULE_START_COL).setCellValue("C");
            dataRow.createCell(SCHEDULE_START_COL + 1).setCellValue("B");
            dataRow.createCell(SCHEDULE_START_COL + 2).setCellValue(4); // 문자열이 아닌 스케줄 셀

            MockMultipartFile file = toMultipartFile(workbook);

            List<DailyShift> actual = excelScheduleParser.parse(file, "손아무개");

            assertThat(actual).containsExactly(new DailyShift(1, "C"));
        }
    }

    @DisplayName("일치하는 성명이 없으면 예외가 발생한다.")
    @Test
    void test_parse_with_unknown_name() throws IOException {
        MockMultipartFile file = excelFile(
                List.of(1, 2, 3),
                new LinkedHashMap<>(Map.of("김아무개", List.of("C", "OFF", "A")))
        );

        assertThatThrownBy(() -> excelScheduleParser.parse(file, "존재하지않는사람"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자의 스케줄 정보가 확인되지 않습니다.");
    }

    @DisplayName("엑셀 파일을 읽는데 실패하면 예외가 발생한다.")
    @Test
    void test_parse_with_io_exception() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        given(file.getInputStream()).willThrow(new IOException("읽기 실패"));

        assertThatThrownBy(() -> excelScheduleParser.parse(file, "김아무개"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("스케줄 파일을 읽어오는데 실패하였습니다.")
                .hasCauseInstanceOf(IOException.class);
    }

    private MockMultipartFile excelFile(List<Integer> days, Map<String, List<String>> people) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            writeHeaderRow(sheet, days);

            int rowNum = SCHEDULE_START_ROW;
            for (Map.Entry<String, List<String>> entry : people.entrySet()) {
                writeDataRow(sheet, rowNum++, entry.getKey(), entry.getValue());
            }

            return toMultipartFile(workbook);
        }
    }

    private void writeHeaderRow(Sheet sheet, List<Integer> days) {
        Row headerRow = sheet.createRow(HEADER_ROW);
        for (int i = 0; i < days.size(); i++) {
            headerRow.createCell(SCHEDULE_START_COL + i).setCellValue(days.get(i));
        }
    }

    private void writeDataRow(Sheet sheet, int rowNum, String name, List<String> shiftCodes) {
        Row dataRow = sheet.createRow(rowNum);
        dataRow.createCell(NAME_COL).setCellValue(name);
        for (int i = 0; i < shiftCodes.size(); i++) {
            dataRow.createCell(SCHEDULE_START_COL + i).setCellValue(shiftCodes.get(i));
        }
    }

    private MockMultipartFile toMultipartFile(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);

        return new MockMultipartFile("file", "schedule.xlsx", CONTENT_TYPE, out.toByteArray());
    }
}
