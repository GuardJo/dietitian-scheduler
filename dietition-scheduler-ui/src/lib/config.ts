/*
런타임 환경 설정 값

API 요청은 항상 same-origin(`/api/...`)으로 나간다.
실제 백엔드로의 중계는 next.config.ts 의 rewrites 프록시가 담당하므로
프론트에는 별도의 API 베이스 URL 설정이 없다. (백엔드 주소는 서버 환경변수 BACKEND_API_URL)

IS_API_MOCKING_ENABLED: 개발 모드에서 기본적으로 켜지며,
`NEXT_PUBLIC_API_MOCKING=disabled` 로 명시적으로 끌 수 있다.
 */
export const IS_API_MOCKING_ENABLED =
    process.env.NODE_ENV === "development" &&
    process.env.NEXT_PUBLIC_API_MOCKING !== "disabled";
