import {useState} from "react";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {scheduleService} from "@/services/schedule-service";

type ScheduleType = "shift" | "personal";

const scheduleTypeOptions: { value: ScheduleType, label: string }[] = [
    {value: "shift", label: "근무 일정"},
    {value: "personal", label: "개인 일정"},
];
const hourOptions = Array.from({length: 24}, (_, index) => index);
const minuteOptions = Array.from({length: 6}, (_, index) => index * 10);
const selectClassName = "h-10 rounded-lg border-2 border-input bg-input px-2 text-[15px] outline-none";
const disabledClassName = "disabled:cursor-not-allowed disabled:opacity-50";

const padTwo = (value: number) => value.toString().padStart(2, "0");

export default function ScheduleRegisterModal({baseYear, baseMonth, isOpen, setIsOpen}: ShiftRegisterModalProps) {
    return (
        <>
            {isOpen &&
                <ScheduleRegisterModalContent key={`${baseYear}-${baseMonth}`} baseYear={baseYear} baseMonth={baseMonth}
                                              setIsOpen={setIsOpen}/>}
        </>
    )
}

function ScheduleRegisterModalContent({baseYear, baseMonth, setIsOpen}: Omit<ShiftRegisterModalProps, "isOpen">) {
    const [scheduleType, setScheduleType] = useState<ScheduleType>("shift");

    return (
        <div className="fixed inset-0 z-30 flex items-end justify-center bg-foreground/35 px-3 pb-14.5"
             role="presentation" onMouseDown={(event) => {
            if (event.target === event.currentTarget) setIsOpen(false)
        }}>
            <section role="dialog" aria-modal="true" aria-labelledby="add-schedule-title"
                     className="w-full max-w-md rounded-2xl bg-card p-5 shadow-xl">
                <div className="flex items-center justify-between"><h2 id="add-schedule-title"
                                                                       className="text-[20px] font-semibold">새 일정
                    등록</h2>
                    <button type="button" onClick={() => setIsOpen(false)} aria-label="Close dialog"
                            className="text-2xl leading-none text-muted-foreground">×
                    </button>
                </div>
                <div role="tablist" aria-label="일정 유형" className="mt-5 grid grid-cols-2 gap-1 rounded-lg bg-input p-1">
                    {scheduleTypeOptions.map(({value, label}) =>
                        <button key={value} type="button" role="tab" aria-selected={scheduleType === value}
                                onClick={() => setScheduleType(value)}
                                className={`h-9 rounded-md text-[14px] font-medium ${scheduleType === value ? "bg-card text-primary shadow-sm" : "text-muted-foreground"}`}>
                            {label}
                        </button>)}
                </div>
                {scheduleType === "shift"
                    ? <ShiftScheduleForm baseYear={baseYear} baseMonth={baseMonth} setIsOpen={setIsOpen}/>
                    : <PersonalScheduleForm baseYear={baseYear} baseMonth={baseMonth} setIsOpen={setIsOpen}/>}
            </section>
        </div>
    )
}

function ShiftScheduleForm({baseYear, baseMonth, setIsOpen}: Omit<ShiftRegisterModalProps, "isOpen">) {
    const monthOptions = Array.from({length: 12}, (_, index) => index + 1);
    const yearOptions = Array.from({length: 7}, (_, index) => baseYear - 3 + index);
    const queryClient = useQueryClient();
    const [uploadedFile, setUploadedFile] = useState<File | null>(null);
    const [selectedYear, setSelectedYear] = useState(baseYear);
    const [selectedMonth, setSelectedMonth] = useState(baseMonth);

    const {mutate: uploadScheduleMutate, isPending} = useMutation({
        mutationKey: ['uploadSchedule', selectedYear, selectedMonth],
        mutationFn: async () => await scheduleService.uploadSchedule(selectedYear, selectedMonth, uploadedFile!),
        onSuccess: () => {
            console.log('schedule uploaded successfully');
            queryClient.invalidateQueries({queryKey: ['schedules', selectedYear, selectedMonth]});
            setIsOpen(false);
        },
        onError: (error) => {
            console.error('Error uploading schedule:', error);
            window.alert('근무 스케줄 업로드에 실패하였습니다.');
        }
    })

    const uploadSchedule = () => {
        uploadScheduleMutate();
        console.log(`uploaded schedule, year ${selectedYear}, month ${selectedMonth}, file ${uploadedFile?.name}`);
    }

    return (
        <>
            <div className="mt-5 grid grid-cols-2 gap-3">
                <label className="flex flex-col gap-1.5 text-[13px] font-medium">연도<select
                    value={selectedYear}
                    onChange={(event) => setSelectedYear(Number(event.target.value))}
                    className={selectClassName}>{yearOptions.map((year) =>
                    <option key={year} value={year}>{year}년</option>)}</select></label>
                <label className="flex flex-col gap-1.5 text-[13px] font-medium">월<select
                    value={selectedMonth}
                    onChange={(event) => setSelectedMonth(Number(event.target.value))}
                    className={selectClassName}>{monthOptions.map((month) =>
                    <option key={month} value={month}>{month}월</option>)}</select></label>
            </div>
            <label htmlFor="schedule-file"
                   className="mt-5 flex cursor-pointer flex-col items-center rounded-xl border-2 border-dashed border-input bg-input px-4 py-5 text-center hover:border-primary"><span
                className="text-[15px] font-medium">Excel 파일 업로드</span><span
                className="mt-1 text-[12px] text-muted-foreground">.xlsx 또는 .xls 파일을 선택하세요</span><input
                id="schedule-file" type="file"
                accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
                className="sr-only"
                onChange={(event) => setUploadedFile(event.target.files?.[0] ?? null)}/>{uploadedFile &&
                <span className="mt-2 max-w-full truncate text-[13px] text-primary">{uploadedFile.name}</span>}
            </label>
            <button type="button" disabled={!uploadedFile || isPending} onClick={uploadSchedule}
                    className="mt-5 h-11 w-full rounded-lg bg-primary text-[16px] font-semibold text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50">등록하기
            </button>
        </>
    )
}

function PersonalScheduleForm({baseYear, baseMonth, setIsOpen}: Omit<ShiftRegisterModalProps, "isOpen">) {
    const queryClient = useQueryClient();
    const defaultDate = `${baseYear}-${padTwo(baseMonth)}-01`;
    const [start, setStart] = useState<DateTimeInput>({date: defaultDate, hour: 9, minute: 0});
    const [end, setEnd] = useState<DateTimeInput>({date: defaultDate, hour: 10, minute: 0});
    const [isAllDay, setIsAllDay] = useState(false);

    // 하루 종일일 경우 시간 정보 없이 시작일 ~ 종료일(포함)로 등록
    const startTime = isAllDay ? null : toTime(start);
    const endTime = isAllDay ? null : toTime(end);
    const isFilled = !!start.date && !!end.date;
    const isValidRange = isFilled && (isAllDay
        ? start.date <= end.date
        : `${start.date}T${startTime}` < `${end.date}T${endTime}`);

    const {mutate: registerPersonalScheduleMutate, isPending} = useMutation({
        mutationKey: ['registerPersonalSchedule', start.date, startTime, end.date, endTime],
        mutationFn: async () => await scheduleService.registerPersonalSchedule({
            startDate: start.date,
            startTime,
            endDate: end.date,
            endTime,
            allDay: isAllDay,
        }),
        onSuccess: () => {
            console.log('personal schedule registered successfully');
            queryClient.invalidateQueries({queryKey: ['schedules']});
            setIsOpen(false);
        },
        onError: (error) => {
            console.error('Error registering personal schedule:', error);
            window.alert('개인 일정 등록에 실패하였습니다.');
        }
    })

    return (
        <>
            <label className="mt-5 flex w-fit cursor-pointer items-center gap-2 text-[14px] font-medium">
                <input type="checkbox" checked={isAllDay} onChange={(event) => setIsAllDay(event.target.checked)}
                       className="size-4 accent-primary"/>하루 종일
            </label>
            <DateTimeField label="시작" value={start} onChange={setStart} isTimeDisabled={isAllDay}/>
            <DateTimeField label="종료" value={end} onChange={setEnd} minDate={start.date} isTimeDisabled={isAllDay}/>
            {isFilled && !isValidRange &&
                <p className="mt-2 text-[12px] text-destructive">{isAllDay ? "종료 일자는 시작 일자 이후여야 합니다." : "종료 일시는 시작 일시 이후여야 합니다."}</p>}
            <button type="button" disabled={!isValidRange || isPending} onClick={() => registerPersonalScheduleMutate()}
                    className="mt-5 h-11 w-full rounded-lg bg-primary text-[16px] font-semibold text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50">등록하기
            </button>
        </>
    )
}

function DateTimeField({label, value, onChange, minDate, isTimeDisabled = false}: DateTimeFieldProps) {
    return (
        <fieldset className="mt-5">
            <legend className="text-[13px] font-medium">{label}</legend>
            <div className="mt-1.5 grid grid-cols-[2fr_1fr_1fr] gap-2">
                <input type="date" aria-label={`${label} 일자`} value={value.date} min={minDate}
                       onChange={(event) => onChange({...value, date: event.target.value})}
                       className={`${selectClassName} min-w-0`}/>
                <select aria-label={`${label} 시`} value={value.hour} disabled={isTimeDisabled}
                        onChange={(event) => onChange({...value, hour: Number(event.target.value)})}
                        className={`${selectClassName} ${disabledClassName}`}>{hourOptions.map((hour) =>
                    <option key={hour} value={hour}>{padTwo(hour)}시</option>)}</select>
                <select aria-label={`${label} 분`} value={value.minute} disabled={isTimeDisabled}
                        onChange={(event) => onChange({...value, minute: Number(event.target.value)})}
                        className={`${selectClassName} ${disabledClassName}`}>{minuteOptions.map((minute) =>
                    <option key={minute} value={minute}>{padTwo(minute)}분</option>)}</select>
            </div>
        </fieldset>
    )
}

// HH:mm 형식 (사전순 비교 가능)
function toTime({hour, minute}: DateTimeInput) {
    return `${padTwo(hour)}:${padTwo(minute)}`;
}

interface DateTimeInput {
    date: string, // yyyy-MM-dd
    hour: number,
    minute: number, // 10분 단위
}

interface DateTimeFieldProps {
    label: string,
    value: DateTimeInput,
    onChange: (value: DateTimeInput) => void,
    minDate?: string,
    isTimeDisabled?: boolean,
}

interface ShiftRegisterModalProps {
    baseYear: number,
    baseMonth: number,
    isOpen: boolean,
    setIsOpen: (isOpen: boolean) => void,
}
