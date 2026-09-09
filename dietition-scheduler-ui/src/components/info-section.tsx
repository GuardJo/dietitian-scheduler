export default function InfoSection({children}: { children: React.ReactNode }) {
    return (
        <section className="rounded-[14px] border-2 border-border bg-card px-5 py-4">
            {children}
        </section>
    )
}