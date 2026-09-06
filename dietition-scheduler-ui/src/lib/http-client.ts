/*
공통 HTTP 클라이언트

- 모든 서비스 객체는 이 모듈의 http 헬퍼를 통해 백엔드와 통신한다.
- JSON 직렬화/역직렬화, 에러 정규화를 담당한다.
- 요청은 항상 same-origin(`/api/...`)으로 나가고, next.config.ts 의 rewrites 가 백엔드로 중계한다.
- 인증은 백엔드가 내려주는 httpOnly 쿠키로 처리되므로 credentials: "include" 로 쿠키를 주고받는다.
  (별도 Authorization 헤더 주입 없음)
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

async function request<TResponse>(path: string, options: RequestOptions = {}): Promise<TResponse> {
    const {body, headers, ...rest} = options;

    const mergedHeaders = new Headers(headers);
    if (body !== undefined) {
        mergedHeaders.set("Content-Type", "application/json");
    }

    const response = await fetch(path, {
        credentials: "include",
        ...rest,
        headers: mergedHeaders,
        body: body === undefined ? undefined : JSON.stringify(body),
    });

    const isJson = response.headers.get("Content-Type")?.includes("application/json");
    const payload = isJson ? await response.json().catch(() => null) : await response.text();

    if (!response.ok) {
        const message =
            (isJson && payload && typeof payload === "object" && "message" in payload
                ? String((payload as {message: unknown}).message)
                : null) ?? `요청이 실패했습니다. (${response.status})`;
        throw new ApiError(response.status, message, payload);
    }

    return payload as TResponse;
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
