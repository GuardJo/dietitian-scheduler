import CalendarHeader from "@/components/calendar-header";
import NavigationBar from "@/components/navigation-bar";

export default function Home() {
    return (
        <main className="min-h-screen bg-[#f5f7ff] text-foreground">
            <CalendarHeader/>
            <h1>dietition-scheduler</h1>
            <NavigationBar/>
        </main>
    );
}
