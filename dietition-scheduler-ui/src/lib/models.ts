export type Shift = 'A' | 'C' | 'B' | 'ALL'
export type ShiftColors = Record<Shift, string>

export const ShiftLabel: Record<Shift, string> = {
    A: '05:30 ~ 15:00',
    C: '08:30 ~ 18:00',
    B: '10:00 ~ 19:30',
    ALL: '05:30 ~ 17:30'
}

export interface PersonalEvent {
    id: number,
    title: string,
    startTime: string | null, // HH:mm, 하루 종일일 경우 null
    endTime: string | null, // HH:mm, 하루 종일일 경우 null
}

export interface CalendarCell {
    day?: number,
    shift?: Shift
}

export interface MonthData {
    year: number,
    month: number,
    label: string;
    shiftCount: number,
    shifts: Record<number, Shift>,
    personalEvents?: Record<number, PersonalEvent[]>
}