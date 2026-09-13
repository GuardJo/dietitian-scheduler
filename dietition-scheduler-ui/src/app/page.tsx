import NavigationBar from "@/components/navigation-bar";
import ScheduleScreen from "@/screens/schedule-screen";

export default function Home() {
    return (
        <main className="min-h-screen bg-[#f5f7ff] text-foreground">
            <ScheduleScreen/>
            <NavigationBar/>
        </main>
    );
}
