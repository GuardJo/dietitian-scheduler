/*
스케줄 관련 API 서비스
 */

import {http} from "@/lib/http-client";
import {MonthData, ShiftColors} from "@/lib/models";

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
    }
};