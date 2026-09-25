package org.github.guardjo.dientitian.scheduler.api.service;

import org.github.guardjo.dientitian.scheduler.api.model.AccountUserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

public interface ScheduleService {
    /**
     * 주어진 연도 및 월에 파일 내 업무 스케줄 데이터들을 저장한다.
     *
     * @param userDetails       요청 사용자 정보
     * @param yesr              연도
     * @param month             월
     * @param shiftScheduleFile 근무 스케줄 엑셀 파일
     * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없는 경우
     * @throws IllegalArgumentException  엑셀 파일 내 특정 사용자의 스케줄이 존재하지 않는 경우
     */
    void saveShiftSchedules(AccountUserDetails userDetails, int yesr, int month, MultipartFile shiftScheduleFile);
}
