import {CalendarCell, MonthData, PersonalEvent, ShiftColors, ShiftLabel} from "@/lib/models";

// 근무 스케줄과 구분되는 개인 일정 칸 색상
const PERSONAL_EVENT_COLOR = '#64748B'

// 하루 종일 일정을 먼저, 이후 시작 시간 순으로 정렬
function sortPersonalEvents(events: PersonalEvent[]): PersonalEvent[] {
    return [...events].sort((a, b) => (a.startTime ?? '').localeCompare(b.startTime ?? ''))
}

function getCalendarWeeks(data: MonthData): CalendarCell[][] {
    const firstDay = new Date(data.year, data.month - 1, 1).getDay()
    const daysInMonth = new Date(data.year, data.month, 0).getDate()
    const cells: CalendarCell[] = Array.from({length: firstDay}, () => ({}))
    for (let day = 1; day <= daysInMonth; day++) cells.push({
        day,
        shift: data.shifts[day],
        personalEvents: sortPersonalEvents(data.personalEvents?.[day] ?? [])
    })
    while (cells.length % 7) cells.push({})
    return Array.from({length: cells.length / 7}, (_, index) => cells.slice(index * 7, index * 7 + 7))
}

export default function CalendarGrid({monthData: data, colors}: CalendarGridProps) {
    const calendarWeeks = getCalendarWeeks(data)

    return (
        <section className="mb-16 overflow-hidden rounded-[14px] border-2 border-border bg-card">
            <div className="grid grid-cols-7 bg-[#eef1f8] text-center text-[15px] font-medium text-muted-foreground">
                {['일', '월', '화', '수', '목', '금', '토'].map((day, index) => <div key={`${day}-${index}`}
                                                                              className="py-2.5">{day}</div>)}
            </div>
            <div className="grid grid-cols-7">
                {calendarWeeks.flatMap((week, weekIndex) => week.map((cell, dayIndex) => (
                    <div key={`${weekIndex}-${dayIndex}`}
                         className="flex min-h-13 min-w-0 flex-col items-end gap-1 border-t-2 border-r-2 border-border bg-card pt-1 pb-1.5 text-right text-[15px]">
                        {cell.day && <span className="w-full px-2">{cell.day}</span>}
                        {cell.shift && <span
                            className="flex w-full flex-col items-center justify-center gap-0.5 border-y-2 px-1.5 py-1"
                            style={{borderColor: colors[cell.shift], backgroundColor: `${colors[cell.shift]}22`}}><span
                            className="h-2.5 w-2.5 shrink-0 rounded-full"
                            style={{backgroundColor: colors[cell.shift]}}/><p
                            className="whitespace-normal wrap-break-word text-center text-[11px] leading-tight">{ShiftLabel[cell.shift]}</p></span>}
                        {cell.personalEvents?.map((event) => (
                            <span key={event.id}
                                  title={event.startTime ? `${event.startTime} ~ ${event.endTime} ${event.title}` : event.title}
                                  className="mx-0.5 block w-[calc(100%-4px)] min-w-0 rounded-sm border-l-[3px] px-1 py-0.5 text-left"
                                  style={{
                                      borderColor: PERSONAL_EVENT_COLOR,
                                      backgroundColor: `${PERSONAL_EVENT_COLOR}22`
                                  }}>
                                <p className="truncate text-[11px] leading-tight">{event.title}</p>
                            </span>
                        ))}
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
