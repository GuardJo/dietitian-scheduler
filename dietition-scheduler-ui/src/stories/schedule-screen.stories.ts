import {Meta, StoryObj} from "@storybook/nextjs-vite";
import ScheduleScreen from "@/screens/schedule-screen";

const meta = {
    title: 'screens/ScheduleScreen',
    component: ScheduleScreen,
} satisfies Meta<typeof ScheduleScreen>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {}
};