export type Shift = 'A' | 'C' | 'B' | 'ALL'
export type ShiftColors = Record<Shift, string>

export const ShiftLabel: Record<Shift, string> = {
    A: '05:30 ~ 15:00',
    C: '08:30 ~ 18:00',
    B: '10:00 ~ 19:30',
    ALL: '05:30 ~ 17:30'
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
    shifts: Record<number, Shift>
}