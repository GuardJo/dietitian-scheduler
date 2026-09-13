"use client"

import CalendarHeader from "@/components/calendar-header";
import ScheduleCountSection from "@/components/schedule-count-section";
import ShiftLegendSection from "@/components/shift-legend-section";
import CalendarGrid from "@/components/calendar-grid";
import {MonthData, Shift, ShiftColors} from "@/lib/models";
import {useQuery} from "@tanstack/react-query";
import {scheduleService} from "@/services/schedule-service";


export default function ScheduleScreen() {
    const year = new Date().getFullYear();
    const month = new Date().getMonth() + 1;
    const defaultData: MonthData = {
        year,
        month,
        label: `${month} ${year}`,
        shiftCount: 0,
        shifts: {}
    };
    const defaultColors: ShiftColors = {
        a: '#0867c9',
        b: '#a9c4ff',
        c: '#626466'
    }

    const {data: scheduleData = defaultData} = useQuery({
        queryFn: () => scheduleService.getSchedules(year, month),
        queryKey: ['schedules', year, month],
    });

    const {data: shiftColors = defaultColors} = useQuery({
        queryFn: () => scheduleService.getShiftColors(),
        queryKey: ['shift-colors'],
    });

    const handleChangeColors = (shift: Shift, color: string) => {
        console.log(`change color: ${shift} -> ${color}`);
        // TODO 추후 색상 변경 기능 연동 예정
    }

    return (
        <>
            <CalendarHeader/>
            <div className="flex flex-col gap-3 px-4 pb-36 pt-4">
                <ScheduleCountSection totalShifts={scheduleData.shiftCount}/>
                <ShiftLegendSection colors={shiftColors} onColorChange={handleChangeColors}/>
                <CalendarGrid monthData={scheduleData} colors={shiftColors}/>
            </div>
        </>
    )
}