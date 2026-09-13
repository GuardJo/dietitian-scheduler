import {Shift, ShiftColors, ShiftLabel} from "@/lib/models";

export default function ShiftLegendSection({colors, onColorChange}: ShiftLegendSectionProps) {
    return (
        <section className="rounded-[14px] border-2 border-border bg-card px-5 py-4">
            <h2 className="text-[15px] font-medium tracking-wide text-muted-foreground">근무 형태</h2>
            <div className="mt-3 flex flex-col gap-2.5">
                {(Object.keys(ShiftLabel) as Shift[]).map((type) => (
                    <div key={type} className="flex items-center gap-2.5 text-[17px] leading-none">
            <span className="relative h-5 w-5 shrink-0 overflow-hidden rounded-full border border-border"
                  style={{backgroundColor: colors[type]}}>
              <input aria-label={`${ShiftLabel[type]} color`} type="color" value={colors[type]}
                     onChange={(event) => onColorChange(type, event.target.value)}
                     className="absolute inset-0 h-full w-full cursor-pointer opacity-0"/>
            </span>
                        <span>{ShiftLabel[type]}</span>
                    </div>
                ))}
            </div>
        </section>
    )
}

interface ShiftLegendSectionProps {
    colors: ShiftColors,
    onColorChange: (shift: Shift, color: string) => void
}