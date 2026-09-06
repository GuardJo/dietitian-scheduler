"use client";

import {useEffect, useState} from "react";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";

import {IS_API_MOCKING_ENABLED} from "@/lib/config";

/*
클라이언트 전역 Provider

- react-query QueryClient 를 앱 전역에 제공한다.
- 로컬 개발 모드에서는 msw 워커 기동이 끝난 뒤에 children 을 렌더링한다.
  (워커 준비 전에 요청이 나가면 모킹이 적용되지 않기 때문)
 */
function createQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: {
                staleTime: 60 * 1000,
                retry: 1,
                refetchOnWindowFocus: false,
            },
            mutations: {
                retry: 0,
            },
        },
    });
}

export default function Providers({children}: {children: React.ReactNode}) {
    const [queryClient] = useState(createQueryClient);
    const [isMockingReady, setIsMockingReady] = useState(!IS_API_MOCKING_ENABLED);

    useEffect(() => {
        if (!IS_API_MOCKING_ENABLED) {
            return;
        }
        let cancelled = false;
        import("@/mocks")
            .then(({enableMocking}) => enableMocking())
            .finally(() => {
                if (!cancelled) {
                    setIsMockingReady(true);
                }
            });
        return () => {
            cancelled = true;
        };
    }, []);

    if (!isMockingReady) {
        return null;
    }

    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
