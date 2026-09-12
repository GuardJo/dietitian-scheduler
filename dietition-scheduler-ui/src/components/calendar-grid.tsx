import {CalendarCell, MonthData, ShiftColors} from "@/lib/models";

function getCalendarWeeks(data: MonthData): CalendarCell[][] {
    const firstDay = new Date(data.year, data.month - 1, 1).getDay()
    const daysInMonth = new Date(data.year, data.month, 0).getDate()
    const cells: CalendarCell[] = Array.from({length: firstDay}, () => ({}))
    for (let day = 1; day <= daysInMonth; day++) cells.push({day, shift: data.shifts[day]})
    while (cells.length % 7) cells.push({})
    return Array.from({length: cells.length / 7}, (_, index) => cells.slice(index * 7, index * 7 + 7))
}

export default function CalendarGrid({monthData: data, colors}: CalendarGridProps) {
    const calendarWeeks = getCalendarWeeks(data)

    return (
        <section className="mb-16 overflow-hidden rounded-[14px] border-2 border-border bg-card">
            <div className="grid grid-cols-7 bg-[#eef1f8] text-center text-[15px] font-medium text-muted-foreground">
                {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((day, index) => <div key={`${day}-${index}`}
                                                                              className="py-2.5">{day}</div>)}
            </div>
            <div className="grid grid-cols-7">
                {calendarWeeks.flatMap((week, weekIndex) => week.map((cell, dayIndex) => (
                    <div key={`${weekIndex}-${dayIndex}`}
                         className="relative h-13 border-t-2 border-r-2 border-border bg-card px-2 pt-1 text-right text-[15px]">
                        {cell.day && <span>{cell.day}</span>}
                        {cell.shift && <span
                            className="absolute bottom-1 left-1/2 flex h-5 w-[calc(100%-6px)] -translate-x-1/2 items-center justify-center rounded-[10px] border-2"
                            style={{borderColor: colors[cell.shift], backgroundColor: `${colors[cell.shift]}22`}}><span
                            className="h-2.5 w-2.5 rounded-full" style={{backgroundColor: colors[cell.shift]}}/></span>}
                    </div>
                )))}
            </div>
        </section>
    )
}

interface CalendarGridProps {
    monthData: MonthData,
    colors: ShiftColors,
}