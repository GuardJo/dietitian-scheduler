export type Shift = 'a' | 'c' | 'b'
export type ShiftColors = Record<Shift, string>

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