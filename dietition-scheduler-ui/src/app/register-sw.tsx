"use client";

import {useEffect} from "react";

/*
서비스 워커 등록

- 프로덕션 빌드에서만 등록한다. 개발 모드에서는 msw 가 /mockServiceWorker.js 를
  별도로 등록하므로, PWA 서비스 워커까지 함께 켜면 두 워커가 같은 scope("/")를
  두고 충돌할 수 있다.
 */
export default function RegisterServiceWorker() {
    useEffect(() => {
        if (process.env.NODE_ENV !== "production") {
            return;
        }
        if (typeof window === "undefined" || !("serviceWorker" in navigator)) {
            return;
        }

        navigator.serviceWorker
            .register("/sw.js")
            .catch((error) => {
                console.error("[sw] 서비스 워커 등록에 실패했습니다.", error);
            });
    }, []);

    return null;
}
