import type {Metadata, Viewport} from "next";
import "./globals.css";
import Providers from "@/app/providers";
import RegisterServiceWorker from "@/app/register-sw";

export const metadata: Metadata = {
    title: "손케줄",
    description: "손선생님을 위한 영양사 스케줄 관리표",
    manifest: "/manifest.webmanifest",
    appleWebApp: {
        capable: true,
        statusBarStyle: "default",
        title: "손케줄",
    },
};

export const viewport: Viewport = {
    colorScheme: "light dark",
    themeColor: [
        {media: "(prefers-color-scheme: light)", color: "white"},
        {media: "(prefers-color-scheme: dark)", color: "black"},
    ],
    width: "device-width",
    initialScale: 1,
}

export default function RootLayout({children}: LayoutProps<"/">) {
    return (
        <html lang="ko" className="bg-background">
        <body className="antialiased">
        <RegisterServiceWorker/>
        <Providers>{children}</Providers>
        </body>
        </html>
    );
}
