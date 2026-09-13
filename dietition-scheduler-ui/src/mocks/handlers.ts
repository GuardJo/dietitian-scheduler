import {http, HttpResponse} from "msw";

import type {LoginRequest, LoginResponse} from "@/services/auth-service";
import {MonthData, ShiftColors} from "@/lib/models";

/*
로컬 개발용 msw 핸들러

실제 백엔드가 준비되기 전까지 프론트 개발에 필요한 응답을 흉내낸다.
로그인 성공 시 백엔드와 동일하게 JWT 를 httpOnly 쿠키(access_token)로 심어준다.
 */

/** 로컬 개발용 임시 계정 */
const MOCK_USER = {
    username: "admin",
    password: "password1!",
};

/** 백엔드가 심어줄 인증 쿠키 이름 (실제 이름에 맞춰 조정) */
const AUTH_COOKIE = "access_token";

/**
 * 서명 검증 없는 더미 JWT 문자열 생성 (header.payload.signature 형태만 유지)
 */
function createMockJwt(username: string): string {
    const base64 = (value: object) =>
        btoa(JSON.stringify(value)).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
    const now = Math.floor(Date.now() / 1000);
    const header = base64({alg: "HS256", typ: "JWT"});
    const body = base64({sub: username, iat: now, exp: now + 60 * 60});
    return `${header}.${body}.mock-signature`;
}

export const handlers = [
    http.post("*/api/auth/login", async ({request}) => {
        const {username, password} = (await request.json()) as LoginRequest;

        // 500 테스트용
        if (username === 'error') {
            return HttpResponse.json(
                {message: "로그인 오류"},
                {status: 500}
            )
        }

        if (username !== MOCK_USER.username || password !== MOCK_USER.password) {
            return HttpResponse.json(
                {message: "아이디 또는 비밀번호가 올바르지 않습니다."},
                {status: 401},
            );
        }

        const body: LoginResponse = {username, name: "관리자"};
        return HttpResponse.json(body, {
            status: 200,
            headers: {
                // mocking 응답에 한해 httpOnly 설정 제거
                "Set-Cookie": `${AUTH_COOKIE}=${createMockJwt(username)}; Path=/; SameSite=Lax; Max-Age=3600`,
            },
        });
    }),
    http.post("*/api/auth/logout", async () => {
        return new HttpResponse(null, {
            status: 204,
            headers: {
                "Set-Cookie": `${AUTH_COOKIE}=; Path=/; SameSite=Lax; Max-Age=0`,
            },
        });
    }),
    http.get("*/api/schedules", async ({request}) => {
        const url = new URL(request.url);
        const params = new URLSearchParams(url.search);
        const month = params.get("month");
        const year = params.get("year");

        const data: MonthData = {
            year: Number(year),
            month: Number(month),
            label: `${month} ${year}`,
            shiftCount: 22,
            shifts: {4: 'a', 5: 'c', 6: 'b', 10: 'a'}
        }

        return HttpResponse.json(data, {
            status: 200
        });
    }),
    http.get("*/api/schedules/shifts/colors", async () => {
        const data: ShiftColors = {
            a: '#0867c9',
            b: '#a9c4ff',
            c: '#626466'
        };

        return HttpResponse.json(data, {
            status: 200
        });
    })
];
