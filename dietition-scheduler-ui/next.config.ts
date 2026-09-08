import type {NextConfig} from "next";

/*
백엔드 API 프록시

브라우저는 항상 same-origin(`/api/...`)으로 요청하고, Next 서버가 이를 백엔드로 중계한다.
이렇게 하면
  - CORS 설정이 불필요하고,
  - 인증 쿠키가 크로스 사이트 쿠키가 아니게 되어 `SameSite=Lax` + `HttpOnly` 만으로 안전하게 동작한다.

로컬 개발 중 msw 모킹이 켜져 있으면 서비스 워커가 브라우저 단에서 먼저 가로채므로
이 프록시까지 요청이 도달하지 않는다. 모킹을 끄면(NEXT_PUBLIC_API_MOCKING=disabled)
그대로 실제 백엔드로 흘러간다.
 */
const BACKEND_API_URL = process.env.BACKEND_API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
    async rewrites() {
        return [
            {
                source: "/api/:path*",
                destination: `${BACKEND_API_URL}/api/:path*`,
            },
        ];
    },
};

export default nextConfig;
