/*
공통 HTTP 클라이언트

- 모든 서비스 객체는 이 모듈의 http 헬퍼를 통해 백엔드와 통신한다.
- JSON 직렬화/역직렬화, 에러 정규화를 담당한다.
- 요청은 항상 same-origin(`/api/...`)으로 나가고, next.config.ts 의 rewrites 가 백엔드로 중계한다.
- 인증은 백엔드가 내려주는 httpOnly 쿠키로 처리되므로 credentials: "include" 로 쿠키를 주고받는다.
  (별도 Authorization 헤더 주입 없음)
- 401 응답을 받으면 refresh 토큰으로 access 토큰 재발급을 1회 시도하고, 성공 시 원 요청을 재시도한다.
  재발급까지 실패하면 ApiError(401) 를 그대로 던지고, 로그인 페이지 이동은 providers.tsx 가 처리한다.
 */
export class ApiError extends Error {
    readonly status: number;
    readonly data: unknown;

    constructor(status: number, message: string, data: unknown) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.data = data;
    }
}

interface RequestOptions extends Omit<RequestInit, "body"> {
    body?: unknown;
}

const REFRESH_PATH = "/api/auth/refresh";

/** 401 이어도 재발급을 시도하지 않는 경로 (무한 루프 방지) */
const NO_REFRESH_PATHS = ["/api/auth/login", REFRESH_PATH];

/*
진행 중인 재발급 요청

여러 요청이 동시에 401 을 받아도 refresh 는 한 번만 나가도록 Promise 를 공유한다.
(refresh 토큰은 rotation 되므로 동시에 두 번 호출하면 두 번째 요청이 재사용 감지에 걸려 로그아웃된다)
 */
let refreshPromise: Promise<boolean> | null = null;

function refreshAccessToken(): Promise<boolean> {
    refreshPromise ??= fetch(REFRESH_PATH, {method: "POST", credentials: "include"})
        .then((response) => response.ok)
        .catch(() => false)
        .finally(() => {
            refreshPromise = null;
        });
    return refreshPromise;
}

function send(path: string, options: RequestOptions): Promise<Response> {
    const {body, headers, ...rest} = options;

    const isFormData = body instanceof FormData;
    const mergedHeaders = new Headers(headers);
    if (body !== undefined && !isFormData) {
        mergedHeaders.set("Content-Type", "application/json");
    }

    return fetch(path, {
        credentials: "include",
        ...rest,
        headers: mergedHeaders,
        body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body),
    });
}

async function parse<TResponse>(response: Response): Promise<TResponse> {
    const isJson = response.headers.get("Content-Type")?.includes("application/json");
    const payload = isJson ? await response.json().catch(() => null) : await response.text();

    if (!response.ok) {
        const message =
            (isJson && payload && typeof payload === "object" && "message" in payload
                ? String((payload as { message: unknown }).message)
                : null) ?? `요청이 실패했습니다. (${response.status})`;
        throw new ApiError(response.status, message, payload);
    }

    return (payload && typeof payload === "object" && "data" in payload ? payload.data : payload) as TResponse;
}

async function request<TResponse>(path: string, options: RequestOptions = {}, isRetry = false): Promise<TResponse> {
    const response = await send(path, options);

    if (response.status === 401 && !isRetry && !NO_REFRESH_PATHS.some((p) => path.startsWith(p))) {
        if (await refreshAccessToken()) {
            return request<TResponse>(path, options, true);
        }
    }

    return parse<TResponse>(response);
}

export const http = {
    get: <T>(path: string, options?: RequestOptions) => request<T>(path, {...options, method: "GET"}),
    post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
        request<T>(path, {...options, method: "POST", body}),
    put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
        request<T>(path, {...options, method: "PUT", body}),
    patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
        request<T>(path, {...options, method: "PATCH", body}),
    delete: <T>(path: string, options?: RequestOptions) =>
        request<T>(path, {...options, method: "DELETE"}),
};
