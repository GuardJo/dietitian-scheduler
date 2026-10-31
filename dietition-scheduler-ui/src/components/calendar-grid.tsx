import {CalendarCell, MonthData, PersonalEvent, ShiftColors, ShiftLabel} from "@/lib/models";

// 근무 스케줄과 구분되는 개인 일정 칸 색상
const PERSONAL_EVENT_COLOR = '#64748B'

// 연속된 일자에 걸쳐 동일한 제목으로 저장된 개인 일정 묶음
interface PersonalEventGroup {
    key: number,
    title: string,
    startDay: number,
    endDay: number,
    events: PersonalEvent[],
}

// 주 단위로 잘린 개인 일정 묶음 (lane: 주 내에서 표시될 줄 번호)
interface PersonalEventSegment {
    group: PersonalEventGroup,
    startCol: number,
    span: number,
    lane: number,
}

// 하루 종일 일정을 먼저, 이후 시작 시간 순으로 정렬
function sortPersonalEvents(events: PersonalEvent[]): PersonalEvent[] {
    return [...events].sort((a, b) => (a.startTime ?? '').localeCompare(b.startTime ?? ''))
}

// 바로 전날 동일한 제목의 일정이 있으면 하나의 묶음으로 이어 붙인다.
function groupPersonalEvents(data: MonthData, daysInMonth: number): PersonalEventGroup[] {
    const groups: PersonalEventGroup[] = []
    let openGroups: PersonalEventGroup[] = []
    for (let day = 1; day <= daysInMonth; day++) {
        const nextOpenGroups: PersonalEventGroup[] = []
        for (const event of sortPersonalEvents(data.personalEvents?.[day] ?? [])) {
            const prevIndex = openGroups.findIndex((group) => group.title === event.title)
            if (prevIndex >= 0) {
                const [group] = openGroups.splice(prevIndex, 1)
                group.endDay = day
                group.events.push(event)
                nextOpenGroups.push(group)
            } else {
                const group = {key: event.id, title: event.title, startDay: day, endDay: day, events: [event]}
                groups.push(group)
                nextOpenGroups.push(group)
            }
        }
        openGroups = nextOpenGroups
    }
    return groups
}

// 주 단위로 일정 묶음을 자르고, 겹치지 않도록 줄(lane)을 배정한다.
function getWeekSegments(week: CalendarCell[], groups: PersonalEventGroup[]): PersonalEventSegment[] {
    const days = week.map((cell) => cell.day).filter((day): day is number => day !== undefined)
    if (!days.length) {
        return []
    }
    const firstDay = days[0]
    const lastDay = days[days.length - 1]
    const firstCol = week.findIndex((cell) => cell.day === firstDay)

    const segments = groups
        .filter((group) => group.startDay <= lastDay && group.endDay >= firstDay)
        .map((group) => {
            const start = Math.max(group.startDay, firstDay)
            const end = Math.min(group.endDay, lastDay)
            return {group, startCol: firstCol + start - firstDay, span: end - start + 1, lane: 0}
        })
        .sort((a, b) => a.startCol - b.startCol || b.span - a.span)

    const laneEnds: number[] = []
    for (const segment of segments) {
        const lane = laneEnds.findIndex((endCol) => endCol < segment.startCol)
        segment.lane = lane >= 0 ? lane : laneEnds.length
        laneEnds[segment.lane] = segment.startCol + segment.span - 1
    }

    return segments
}

function getEventTooltip(group: PersonalEventGroup): string {
    const [first] = group.events
    const last = group.events[group.events.length - 1]
    const period = group.startDay === group.endDay ? `${group.startDay}일` : `${group.startDay}일 ~ ${group.endDay}일`
    const time = first.startTime ? ` ${first.startTime} ~ ${last.endTime}` : ''

    return `${group.title} (${period}${time})`
}

function getCalendarWeeks(data: MonthData, daysInMonth: number): CalendarCell[][] {
    const firstDay = new Date(data.year, data.month - 1, 1).getDay()
    const cells: CalendarCell[] = Array.from({length: firstDay}, () => ({}))

    for (let day = 1; day <= daysInMonth; day++) {
        cells.push({day, shift: data.shifts[day]})
    }

    while (cells.length % 7) {
        cells.push({})
    }
    return Array.from({length: cells.length / 7}, (_, index) => cells.slice(index * 7, index * 7 + 7))
}

export default function CalendarGrid({monthData: data, colors}: CalendarGridProps) {
    const daysInMonth = new Date(data.year, data.month, 0).getDate()
    const calendarWeeks = getCalendarWeeks(data, daysInMonth)
    const eventGroups = groupPersonalEvents(data, daysInMonth)

    return (
        <section className="mb-16 overflow-hidden rounded-[14px] border-2 border-border bg-card">
            <div className="grid grid-cols-7 bg-[#eef1f8] text-center text-[15px] font-medium text-muted-foreground">
                {['일', '월', '화', '수', '목', '금', '토'].map((day, index) => <div key={`${day}-${index}`}
                                                                              className="py-2.5">{day}</div>)}
            </div>
            {calendarWeeks.map((week, weekIndex) => {
                const segments = getWeekSegments(week, eventGroups)
                const laneCount = segments.reduce((max, segment) => Math.max(max, segment.lane + 1), 0)

                // 1행: 일자, 2행: 근무 스케줄, 3행~: 개인 일정, 마지막 행: 하단 여백
                return (
                    <div key={weekIndex} className="grid min-h-13 grid-cols-7 text-[15px]"
                         style={{gridTemplateRows: `auto auto repeat(${laneCount}, auto) minmax(6px, 1fr)`}}>
                        {week.map((cell, dayIndex) => (
                            <div key={`bg-${dayIndex}`} className="border-t-2 border-r-2 border-border bg-card"
                                 style={{gridColumn: dayIndex + 1, gridRow: '1 / -1'}}/>
                        ))}
                        {week.map((cell, dayIndex) => cell.day && (
                            <span key={`day-${dayIndex}`} className="px-2 pt-1 text-right"
                                  style={{gridColumn: dayIndex + 1, gridRow: 1}}>{cell.day}</span>
                        ))}
                        {week.map((cell, dayIndex) => cell.shift && (
                            <span key={`shift-${dayIndex}`}
                                  className="mt-1 flex min-w-0 flex-col items-center justify-center gap-0.5 border-y-2 px-1.5 py-1"
                                  style={{
                                      gridColumn: dayIndex + 1,
                                      gridRow: 2,
                                      borderColor: colors[cell.shift],
                                      backgroundColor: `${colors[cell.shift]}22`
                                  }}><span
                                className="h-2.5 w-2.5 shrink-0 rounded-full"
                                style={{backgroundColor: colors[cell.shift]}}/><p
                                className="whitespace-normal wrap-break-word text-center text-[11px] leading-tight">{ShiftLabel[cell.shift]}</p></span>
                        ))}
                        {segments.map(({group, startCol, span, lane}) => {
                            const isStart = week[startCol].day === group.startDay
                            const isEnd = week[startCol + span - 1].day === group.endDay
                            return (
                                <span key={`event-${group.key}`} title={getEventTooltip(group)}
                                      className={`mt-1 block min-w-0 px-1 py-0.5 text-left ${isStart ? 'ml-0.5 rounded-l-sm border-l-[3px]' : ''} ${isEnd ? 'mr-0.5 rounded-r-sm' : ''}`}
                                      style={{
                                          gridColumn: `${startCol + 1} / span ${span}`,
                                          gridRow: lane + 3,
                                          borderColor: PERSONAL_EVENT_COLOR,
                                          // 칸 경계선이 비치지 않도록 불투명한 색으로 섞는다.
                                          backgroundColor: `color-mix(in srgb, ${PERSONAL_EVENT_COLOR} 13%, var(--card))`
                                      }}>
                                    <p className="truncate text-[11px] leading-tight">{group.title}</p>
                                </span>
                            )
                        })}
                    </div>
                )
            })}
        </section>
    )
}

interface CalendarGridProps {
    monthData: MonthData,
    colors: ShiftColors,
}
