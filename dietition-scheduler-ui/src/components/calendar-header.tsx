import {ChevronDown, Menu} from "lucide-react";
import {useState} from "react";

export default function CalendarHeader() {
    const currentYear = new Date().getFullYear()
    const currentMonth = new Date().getMonth() + 1

    const [selectedYear, setSelectedYear] = useState(currentYear.valueOf())
    const [openMenu, setOpenMenu] = useState<'year' | 'month' | null>(null)
    const [selectedMonthNumber, setSelectedMonthNumber] = useState(currentMonth.valueOf())
    const monthOptions = Array.from({length: 12}, (_, index) => index + 1)
    const yearOptions = Array.from({length: 7}, (_, index) => currentYear - 3 + index)
    const [isAccountMenuOpen, setIsAccountMenuOpen] = useState(false)

    const toggleMenu = (menu: 'year' | 'month') =>
        setOpenMenu((current) => current === menu ? null : menu)

    const onLogout = () => {
        // TODO API 연동
        console.log('logout clicked')
    }

    return (
        <header
            className="sticky top-0 z-20 flex h-14.5 items-center justify-between border-b-2 border-[#c4cadd] bg-[#f5f7ff] px-4">
            <div className="flex items-center gap-1">
                <div className="relative">
                    <button type="button" onClick={() => toggleMenu('month')}
                            className="flex items-center gap-1 text-[22px] font-semibold tracking-[-0.03em]"
                            aria-expanded={openMenu === 'month'} aria-haspopup="listbox"
                            aria-label="Select month">{new Intl.DateTimeFormat('en-US', {month: 'long'}).format(new Date(selectedYear, selectedMonthNumber - 1, 1))}<ChevronDown
                        className={`h-5 w-5 text-muted-foreground transition-transform ${openMenu === 'month' ? 'rotate-180' : ''}`}/>
                    </button>
                    {openMenu === 'month' && <div
                        className="absolute left-0 top-11 z-20 grid w-44 grid-cols-3 gap-1 rounded-xl border-2 border-border bg-card p-2 shadow-lg"
                        role="listbox" aria-label="Available months">
                        {monthOptions.map((month) => <button key={month} type="button" role="option"
                                                             aria-selected={selectedMonthNumber === month}
                                                             onClick={() => {
                                                                 setSelectedMonthNumber(month);
                                                                 setOpenMenu(null)
                                                             }}
                                                             className={`rounded-lg px-2 py-2 text-[14px] ${selectedMonthNumber === month ? 'bg-primary/10 font-semibold text-primary' : 'hover:bg-muted'}`}>{month}월</button>)}
                    </div>}
                </div>
                <div className="relative">
                    <button type="button" onClick={() => toggleMenu('year')}
                            className="flex items-center gap-1 text-[22px] font-semibold tracking-[-0.03em]"
                            aria-expanded={openMenu === 'year'} aria-haspopup="listbox"
                            aria-label="Select year">{selectedYear}<ChevronDown
                        className={`h-5 w-5 text-muted-foreground transition-transform ${openMenu === 'year' ? 'rotate-180' : ''}`}/>
                    </button>
                    {openMenu === 'year' && <div
                        className="absolute left-0 top-11 z-20 w-32 rounded-xl border-2 border-border bg-card p-2 shadow-lg"
                        role="listbox" aria-label="Available years">
                        {yearOptions.map((year) => <button key={year} type="button" role="option"
                                                           aria-selected={selectedYear === year} onClick={() => {
                            setSelectedYear(year);
                            setOpenMenu(null)
                        }}
                                                           className={`block w-full rounded-lg px-3 py-2 text-left text-[14px] ${selectedYear === year ? 'bg-primary/10 font-semibold text-primary' : 'hover:bg-muted'}`}>{year}년</button>)}
                    </div>}
                </div>
            </div>
            <div className="relative">
                <button type="button" onClick={() => setIsAccountMenuOpen((open) => !open)} aria-label="Open menu"
                        aria-expanded={isAccountMenuOpen} aria-haspopup="menu"
                        className="flex h-9 w-9 items-center justify-center"><Menu
                    className="h-7 w-7 text-muted-foreground"/></button>
                {isAccountMenuOpen && <div role="menu"
                                           className="absolute right-0 top-11 z-30 w-32 rounded-xl border-2 border-border bg-card p-1.5 shadow-lg">
                    <button type="button" role="menuitem" onClick={onLogout}
                            className="w-full rounded-lg px-3 py-2 text-left text-[14px] text-foreground hover:bg-muted">로그아웃
                    </button>
                </div>}
            </div>
        </header>
    )
}
