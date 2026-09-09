import {Meta, StoryObj} from "@storybook/nextjs-vite";
import ScheduleCountSection from "@/components/schedule-count-section";

const meta = {
    title: "components/ScheduleCountSection",
    component: ScheduleCountSection,
} satisfies Meta<typeof ScheduleCountSection>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {
        totalShifts: 20,
    }
}