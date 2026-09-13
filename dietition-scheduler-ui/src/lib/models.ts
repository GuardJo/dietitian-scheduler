export type Shift = 'a' | 'c' | 'b'
export type ShiftColors = Record<Shift, string>

export const ShiftLabel: Record<Shift, string> = {
    a: '05:30 ~ 15:00',
    c: '08:30 ~ 18:00',
    b: '10:00 ~ 19:30'
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