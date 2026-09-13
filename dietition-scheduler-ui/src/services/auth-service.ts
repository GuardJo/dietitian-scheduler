import {http} from "@/lib/http-client";

/*
인증 관련 API 서비스 객체

컴포넌트/훅은 이 객체를 통해서만 인증 API 를 호출한다.
엔드포인트, 요청/응답 스키마 변경은 이 파일 안에서 흡수한다.

인증 방식
- 로그인 성공 시 백엔드가 JWT 를 httpOnly 쿠키로 내려준다. (응답 본문에는 토큰이 없다)
- 이후 요청은 http-client 의 credentials: "include" 설정으로 쿠키가 자동 전송된다.
- 로그아웃은 백엔드가 해당 쿠키를 만료시키는 방식으로 처리한다.
 */
export interface LoginRequest {
    username: string;
    password: string;
}

/** 로그인 성공 응답 본문. 토큰은 쿠키로 전달되므로 여기엔 포함되지 않는다. */
export interface LoginResponse {
    username: string;
    name?: string;
}

export const authService = {
    login(payload: LoginRequest): Promise<LoginResponse> {
        return http.post<LoginResponse>("/api/auth/login", payload);
    },
    logout(): Promise<void> {
        return http.post("/api/auth/logout");
    },
};
