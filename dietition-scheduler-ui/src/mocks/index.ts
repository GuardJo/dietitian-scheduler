/*
브라우저 환경에서 msw 워커를 시작하는 진입점

Providers 에서 개발 모드일 때만 동적 import 하여 호출한다.
 */
export async function enableMocking(): Promise<void> {
    if (typeof window === "undefined") {
        return;
    }

    const {worker} = await import("@/mocks/browser");
    await worker.start({
        onUnhandledRequest: "bypass",
    });
}
