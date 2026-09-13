import type {MetadataRoute} from "next";

export default function manifest(): MetadataRoute.Manifest {
    return {
        name: "손케줄 - 영양사 스케줄 관리표",
        short_name: "손케줄",
        description: "손선생님을 위한 영양사 스케줄 관리표",
        start_url: "/",
        display: "fullscreen",
        background_color: "#c4cadd",
        theme_color: "#0861c4",
        lang: "ko",
        icons: [
            {
                src: "/icons/icon-192.png",
                sizes: "192x192",
                type: "image/png",
                purpose: "any",
            },
            {
                src: "/icons/icon-512.png",
                sizes: "512x512",
                type: "image/png",
                purpose: "any",
            },
        ],
    };
}
