package org.github.guardjo.dientitian.scheduler.api.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.github.guardjo.dientitian.scheduler.api.exception.ExcelFileReadException;
import org.github.guardjo.dientitian.scheduler.api.model.dto.DailyShift;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ExcelScheduleParser {
    private static final int HEADER_ROW = 12; // 엑셀 헤더 (13행)
    private static final int SCHEDULE_START_ROW = 14; // 스케줄 시작 행 (15행)
    private static final int NAME_COL = 3; // 성명 열 (D열)
    private static final int SCHEDULE_START_COL = 4; // 스케줄 시작 열 (E열)

    /**
     * 주어진 근무 스케줄 엑셀 파일에서 근무 스케줄 정보를 추출한다.
     *
     * @param excelFile 근무 스케줄 엑셀 파일
     * @param name      근무자 성명
     * @return 엑셀파일에서 추출한 근무 스케줄 데이터 목록
     * @throws ExcelFileReadException   스케줄 파일 읽기에 실패한 경우
     * @throws IllegalArgumentException 사용자의 스케줄 정보가 확인되지 않는 경우
     */
    public List<DailyShift> parse(MultipartFile excelFile, String name) {
        log.info("Parse excel file, fileName = {}", excelFile.getOriginalFilename());

        try (InputStream inputStream = excelFile.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<DailyShift> dailyShifts = extractRowsByName(sheet, name);
            log.info("Parsed excel file, name = {}, shifts = {}", name, dailyShifts.size());

            return dailyShifts;
        } catch (IOException e) {
            log.error("Failed read excel file, cause = {}", e.getMessage(), e);
            throw new ExcelFileReadException("스케줄 파일을 읽어오는데 실패하였습니다.", e);
        }
    }

    private List<DailyShift> extractRowsByName(Sheet sheet, String name) {
        for (int rowNum = SCHEDULE_START_ROW; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row dataRow = sheet.getRow(rowNum);

            if (Objects.isNull(dataRow)) {
                continue;
            }

            Cell nameCell = dataRow.getCell(NAME_COL);
            if (Objects.isNull(nameCell) || !name.equals(nameCell.getStringCellValue())) {
                continue;
            }

            return extractShifts(sheet, dataRow);
        }

        log.warn("Not found schedule, name = {}", name);
        throw new IllegalArgumentException("사용자의 스케줄 정보가 확인되지 않습니다.");
    }

    private List<DailyShift> extractShifts(Sheet sheet, Row dataRow) {
        Row headerRow = sheet.getRow(HEADER_ROW);
        List<DailyShift> dailyShifts = new ArrayList<>();

        for (int colNum = SCHEDULE_START_COL; colNum <= headerRow.getLastCellNum(); colNum++) {
            Cell dayCell = headerRow.getCell(colNum);
            if (Objects.isNull(dayCell) || dayCell.getCellType() != CellType.NUMERIC) {
                continue;
            }

            int day = (int) dayCell.getNumericCellValue();
            Cell shiftCell = dataRow.getCell(colNum);

            if (Objects.isNull(shiftCell) || shiftCell.getCellType() != CellType.STRING) {
                continue;
            }
            String shiftCode = shiftCell.getStringCellValue();

            dailyShifts.add(new DailyShift(day, shiftCode));
        }

        return dailyShifts;
    }
}
