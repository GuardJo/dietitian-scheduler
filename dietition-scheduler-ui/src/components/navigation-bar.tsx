"use client"

import {CalendarDays, UserRound, UsersRound} from "lucide-react";
import {useRouter} from "next/navigation";

export default function NavigationBar() {
    const router = useRouter()
    const clickSchedule = () => {
        router.push('/')
    }

    const clickMenu = () => {
        alert('추후 오픈 예정입니다.')
    }

    return (
        <nav
            className="fixed bottom-0 left-0 right-0 flex h-14.5 items-center justify-around border-t-2 border-[#c4cadd] bg-[#f5f7ff] px-4"
            aria-label="Main navigation">
            <button type="button" onClick={clickSchedule}
                    className="flex flex-col items-center gap-0.5 text-[13px] leading-tight text-primary">
                <CalendarDays className="h-5 w-5"/>Schedule
            </button>
            <button type="button" onClick={clickMenu}
                    className="flex flex-col items-center gap-0.5 text-[13px] leading-tight text-[#626466]">
                <UsersRound className="h-5 w-5"/>Patients
            </button>
            <button type="button" onClick={clickMenu}
                    className="flex flex-col items-center gap-0.5 text-[13px] leading-tight text-[#626466]">
                <UserRound className="h-5 w-5"/>Profile
            </button>
        </nav>
    )
}