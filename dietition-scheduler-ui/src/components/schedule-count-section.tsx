import InfoSection from "@/components/info-section";

export default function ScheduleCountSection({totalShifts = 0}: ScheduleCountProps) {
    return (
        <InfoSection>
            <h2 className="text-[15px] font-medium tracking-wide text-muted-foreground">THIS MONTH</h2>
            <div className="mt-3 flex items-baseline gap-2"><strong
                className="text-[52px] font-bold leading-none text-primary">{totalShifts}</strong><span
                className="text-[17px]">Total Shifts</span></div>
        </InfoSection>
    )
}

interface ScheduleCountProps {
    totalShifts: number;
}