package org.github.guardjo.dientitian.scheduler.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface ScheduleService {
    /**
     * 주어진 연도 및 월에 파일 내 업무 스케줄 데이터들을 저장한다.
     *
     * @param userId            요청한 사용자 식별키
     * @param yesr              연도
     * @param month             월
     * @param shiftScheduleFile 근무 스케줄 엑셀 파일
     */
    void saveShiftSchedules(Long userId, int yesr, int month, MultipartFile shiftScheduleFile);
}
