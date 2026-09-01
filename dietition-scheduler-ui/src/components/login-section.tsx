import {BriefcaseMedical, LockKeyhole, User2} from "lucide-react";
import {useRouter} from "next/navigation";
import React from "react";

/*
로그인 섹션 컴포넌트
 */
export default function LoginSection() {
    const router = useRouter();

    const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        const formData = new FormData(e.currentTarget);
        console.log(`Logged in ${formData.get("id")}`);
        // TODO API 연동
        router.push("/");
    }

    const handleContactAdmin = () => {
        alert("저한테 직접 얘기하세요.");
    }

    return (
        <section
            className="mx-auto flex min-h-[calc(100vh-1.75rem)] w-full max-w-179 flex-col rounded-[14px] border-2 border-border bg-card px-5 py-6 shadow-sm sm:min-h-[1040px] sm:rounded-2xl sm:px-12 sm:py-16">
            <div className="flex flex-col items-center">
                <div
                    className="flex h-11 w-11 items-center justify-center rounded-[3px] bg-primary sm:h-24 sm:w-24 sm:rounded-md">
                    <BriefcaseMedical aria-hidden="true"
                                      className="h-6 w-6 stroke-[2.5] text-primary-foreground sm:h-12 sm:w-12"/>
                </div>
                <h1 className="mt-6 text-center text-[30px] font-bold leading-none tracking-[-0.045em] text-foreground sm:mt-12 sm:text-6xl">손케줄</h1>
                <p className="mt-2.5 text-center text-[18px] leading-tight text-muted-foreground sm:mt-4 sm:text-[30px]">영양사
                    스케줄 관리</p>
            </div>

            <form className="mt-14 flex flex-col gap-7 sm:mt-24 sm:gap-12" onSubmit={handleSubmit}>
                <div className="flex flex-col gap-4">
                    <label
                        className="text-[15px] font-medium leading-tight tracking-wide text-foreground sm:text-2xl">아이디</label>
                    <div className="relative">
                        <User2 aria-hidden="true"
                               className="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground sm:left-7 sm:h-10 sm:w-10"/>
                        <input id="id" name="id" type="text" placeholder="아이디를 입력하세요."
                               className="h-12 w-full rounded-[10px] border-2 border-input bg-input px-3 pl-10 text-[16px] text-foreground outline-none placeholder:text-muted-foreground focus:border-primary focus:ring-2 focus:ring-primary/20 sm:h-24 sm:rounded-2xl sm:pl-24 sm:text-[28px]"/>
                    </div>
                </div>
                <div className="flex flex-col gap-4">
                    <div className="flex items-center justify-between gap-3">
                        <label htmlFor="password"
                               className="text-[15px] font-medium leading-tight tracking-wide text-foreground sm:text-2xl">비밀번호</label>
                    </div>
                    <div className="relative">
                        <LockKeyhole aria-hidden="true"
                                     className="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground sm:left-7 sm:h-10 sm:w-10"/>
                        <input id="password" name="password" type="password"
                               autoComplete="current-password" placeholder="비밀번호를 입력하세요."
                               className="h-12 w-full rounded-[10px] border-2 border-input bg-input px-3 pl-10 text-[16px] text-foreground outline-none placeholder:text-muted-foreground focus:border-primary focus:ring-2 focus:ring-primary/20 sm:h-24 sm:rounded-2xl sm:pl-24 sm:text-[28px]"/>
                    </div>
                </div>
                <button type="submit"
                        className="h-12 rounded-[10px] bg-primary text-[20px] font-semibold text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-primary/25 sm:h-24 sm:rounded-2xl sm:text-[32px]">Sign
                    In
                </button>
            </form>
            <p className="mt-auto pt-7 text-center text-[15px] leading-relaxed text-muted-foreground sm:pt-14 sm:text-2xl">비밀번호를
                잊어버리셨습니까? <button type="button" className="text-primary hover:underline"
                                  onClick={handleContactAdmin}>Contact Admin</button>
            </p>
        </section>
    )
}