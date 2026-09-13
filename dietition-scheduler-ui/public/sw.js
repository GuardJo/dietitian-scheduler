/*
PWA 서비스 워커

- 프로덕션 빌드에서만 등록된다 (src/app/register-sw.tsx 참고).
- 정적 자산(_next/static, 아이콘 등)은 cache-first, 그 외 페이지 네비게이션 등은
  network-first로 처리해 오프라인에서도 마지막으로 성공한 응답을 보여준다.
- /api/* 는 캐싱하지 않고 항상 네트워크로만 처리한다. Cache Storage는 쿠키/세션이 아닌
  origin 단위로 저장되므로, 캐싱하면 같은 기기를 공유하는 다른 사용자에게 이전 사용자의
  스케줄 응답이 노출될 수 있기 때문이다.
*/
const CACHE_NAME = "sonkedule-cache-v1";

self.addEventListener("install", () => {
    self.skipWaiting();
});

self.addEventListener("activate", (event) => {
    event.waitUntil(
        caches.keys().then((keys) =>
            Promise.all(
                keys
                    .filter((key) => key !== CACHE_NAME)
                    .map((key) => caches.delete(key))
            )
        )
    );
    event.waitUntil(self.clients.claims())
});

function isStaticAsset(url) {
    return (
        url.pathname.startsWith("/_next/static/") ||
        url.pathname.startsWith("/icons/")
    );
}

function isApiRequest(url) {
    return url.pathname.startsWith("/api/");
}

self.addEventListener("fetch", (event) => {
    const request = event.request;
    if (request.method !== "GET") {
        return;
    }

    const url = new URL(request.url);
    if (url.origin !== self.location.origin) {
        return;
    }

    if (isApiRequest(url)) {
        return;
    }

    if (isStaticAsset(url)) {
        event.respondWith(
            caches.open(CACHE_NAME).then(async (cache) => {
                const cached = await cache.match(request);
                if (cached) {
                    return cached;
                }
                const response = await fetch(request);
                cache.put(request, response.clone());
                return response;
            })
        );
        return;
    }

    event.respondWith(
        fetch(request)
            .then((response) => {
                caches.open(CACHE_NAME).then((cache) => cache.put(request, response.clone()));
                return response;
            })
            .catch(() => caches.match(request))
    );
});
