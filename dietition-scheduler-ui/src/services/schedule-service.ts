/*
스케줄 관련 API 서비스
 */

import {http} from "@/lib/http-client";
import {MonthData, Shift, ShiftColors} from "@/lib/models";

export interface ShiftColorChangeRequest {
    shiftType: Shift;
    color: string;
}

export interface PersonalScheduleRegisterRequest {
    title: string;
    memo: string | null; // 선택 입력
    startDate: string; // yyyy-MM-dd
    startTime: string | null; // HH:mm, 하루 종일일 경우 null
    endDate: string; // yyyy-MM-dd
    endTime: string | null; // HH:mm, 하루 종일일 경우 null
    allDay: boolean;
}

export const scheduleService = {
    getSchedules(year: number, month: number): Promise<MonthData> {
        return http.get(`/api/schedules?year=${year}&month=${month}`);
    },
    getShiftColors(): Promise<ShiftColors> {
        return http.get(`/api/schedules/shifts/colors`);
    },
    uploadSchedule(year: number, month: number, file: File): Promise<string> {
        const formData = new FormData();
        formData.append("year", year.toString());
        formData.append("month", month.toString());
        formData.append("file", file);

        return http.post("/api/schedules", formData);
    },
    registerPersonalSchedule(registerReq: PersonalScheduleRegisterRequest): Promise<void> {
        return http.post("/api/schedules/personal", {
            title: registerReq.title,
            memo: registerReq.memo,
            startDate: registerReq.startDate,
            startTime: registerReq.startTime,
            endDate: registerReq.endDate,
            endTime: registerReq.endTime,
            allDay: registerReq.allDay,
        });
    },
    updateShiftColor(changeReq: ShiftColorChangeRequest): Promise<void> {
        return http.patch(`/api/schedules/shifts/colors`, {
            shiftType: changeReq.shiftType,
            color: changeReq.color,
        });
    }
};