import {useState} from "react";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {scheduleService} from "@/services/schedule-service";

export default function ShiftRegisterModal({baseYear, baseMonth, isOpen, setIsOpen}: ShiftRegisterModalProps) {
    const monthOptions = Array.from({length: 12}, (_, index) => index + 1);
    const yearOptions = Array.from({length: 7}, (_, index) => baseYear - 3 + index);
    const queryClient = useQueryClient();
    const [uploadedFile, setUploadedFile] = useState<File | null>(null);
    const [selectedYear, setSelectedYear] = useState(baseYear);
    const [selectedMonth, setSelectedMonth] = useState(baseMonth);

    const {mutate: uploadScheduleMutate} = useMutation({
        mutationKey: ['uploadSchedule', selectedYear, selectedMonth],
        mutationFn: async () => await scheduleService.uploadSchedule(selectedYear, selectedMonth, uploadedFile!),
        onSuccess: () => {
            console.log('schedule uploaded successfully');
            queryClient.invalidateQueries({queryKey: ['schedules', selectedYear, selectedMonth]});
            setIsOpen(false);
            setUploadedFile(null);
            setSelectedYear(baseYear);
            setSelectedMonth(baseMonth);
        },
        onError: (error) => {
            console.error('Error uploading schedule:', error);
        }
    })

    const uploadSchedule = () => {
        uploadScheduleMutate();
        console.log(`uploaded schedule, year ${selectedYear}, month ${selectedMonth}, file ${uploadedFile?.name}`);
    }

    return (
        <>
            {isOpen && <div className="fixed inset-0 z-30 flex items-end justify-center bg-foreground/35 px-3 pb-14.5"
                            role="presentation" onMouseDown={(event) => {
                if (event.target === event.currentTarget) setIsOpen(false)
            }}>
                <section role="dialog" aria-modal="true" aria-labelledby="add-schedule-title"
                         className="w-full max-w-md rounded-2xl bg-card p-5 shadow-xl">
                    <div className="flex items-center justify-between"><h2 id="add-schedule-title"
                                                                           className="text-[20px] font-semibold">새 근무
                        스케줄 등록</h2>
                        <button type="button" onClick={() => setIsOpen(false)} aria-label="Close dialog"
                                className="text-2xl leading-none text-muted-foreground">×
                        </button>
                    </div>
                    <div className="mt-5 grid grid-cols-2 gap-3">
                        <label className="flex flex-col gap-1.5 text-[13px] font-medium">연도<select
                            value={selectedYear}
                            onChange={(event) => setSelectedYear(Number(event.target.value))}
                            className="h-10 rounded-lg border-2 border-input bg-input px-2 text-[15px] outline-none">{yearOptions.map((year) =>
                            <option key={year} value={year}>{year}년</option>)}</select></label>
                        <label className="flex flex-col gap-1.5 text-[13px] font-medium">월<select
                            value={selectedMonth}
                            onChange={(event) => setSelectedMonth(Number(event.target.value))}
                            className="h-10 rounded-lg border-2 border-input bg-input px-2 text-[15px] outline-none">{monthOptions.map((month) =>
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
                    <button type="button" disabled={!uploadedFile} onClick={uploadSchedule}
                            className="mt-5 h-11 w-full rounded-lg bg-primary text-[16px] font-semibold text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50">등록하기
                    </button>
                </section>
            </div>}
        </>
    )
}

interface ShiftRegisterModalProps {
    baseYear: number,
    baseMonth: number,
    isOpen: boolean,
    setIsOpen: (isOpen: boolean) => void,
}