/*
브라우저 환경에서 msw 워커를 시작하는 진입점

Providers 에서 개발 모드일 때만 동적 import 하여 호출한다.
 */
const globalRef = globalThis as typeof globalThis & {
    __mswStartPromise?: Promise<void>;
};

export function enableMocking(): Promise<void> {
    if (typeof window === "undefined") {
        return Promise.resolve();
    }

    globalRef.__mswStartPromise ??= import("@/mocks/browser")
        .then(({worker}) => worker.start({onUnhandledRequest: "bypass"}))
        .then(() => undefined);

    return globalRef.__mswStartPromise;
}
