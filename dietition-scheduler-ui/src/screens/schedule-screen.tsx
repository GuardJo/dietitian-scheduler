"use client"

import CalendarHeader from "@/components/calendar-header";
import ScheduleCountSection from "@/components/schedule-count-section";
import ShiftLegendSection from "@/components/shift-legend-section";
import CalendarGrid from "@/components/calendar-grid";
import {MonthData, Shift, ShiftColors} from "@/lib/models";
import {useQuery} from "@tanstack/react-query";
import {scheduleService} from "@/services/schedule-service";
import {useState} from "react";
import {Plus} from "lucide-react";
import ShiftRegisterModal from "@/components/shift-register-modal";


export default function ScheduleScreen() {
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;
    const defaultData: MonthData = {
        year: currentYear,
        month: currentMonth,
        label: `${currentMonth} ${currentYear}`,
        shiftCount: 0,
        shifts: {}
    };
    const defaultColors: ShiftColors = {
        a: '#2563EB',
        b: '#F59E0B',
        c: '#8B5CF6'
    }

    const [selectedYear, setSelectedYear] = useState(currentYear);
    const [selectedMonth, setSelectedMonth] = useState(currentMonth);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);

    const {data: scheduleData = defaultData} = useQuery({
        queryFn: () => scheduleService.getSchedules(selectedYear, selectedMonth),
        queryKey: ['schedules', selectedYear, selectedMonth],
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
            <CalendarHeader selectedYear={selectedYear} selectedMonthNumber={selectedMonth}
                            onYearChange={setSelectedYear} onMonthChange={setSelectedMonth}/>
            <div className="flex flex-col gap-3 px-4 pb-36 pt-4">
                <ScheduleCountSection totalShifts={scheduleData.shiftCount}/>
                <ShiftLegendSection colors={shiftColors} onColorChange={handleChangeColors}/>
                <CalendarGrid monthData={scheduleData} colors={shiftColors}/>
                <button type="button" onClick={() => setIsAddModalOpen(true)} aria-label="Add shift"
                        className="fixed bottom-18 right-3 flex h-12 w-12 items-center justify-center rounded-[18px] bg-primary text-primary-foreground shadow-lg">
                    <Plus className="h-6 w-6"/></button>
                <ShiftRegisterModal baseYear={selectedYear} baseMonth={selectedMonth} isOpen={isAddModalOpen}
                                    setIsOpen={setIsAddModalOpen}/>
            </div>
        </>
    )
}