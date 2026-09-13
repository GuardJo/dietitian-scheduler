/*
PWA 서비스 워커

- 프로덕션 빌드에서만 등록된다 (src/app/register-sw.tsx 참고).
- 정적 자산(_next/static, 아이콘 등)은 cache-first, 그 외 요청(API, 페이지 네비게이션)은
  network-first로 처리해 오프라인에서도 마지막으로 성공한 응답을 보여준다.
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
    self.clients.claim();
});

function isStaticAsset(url) {
    return (
        url.pathname.startsWith("/_next/static/") ||
        url.pathname.startsWith("/icons/")
    );
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
