import {Meta, StoryObj} from "@storybook/nextjs-vite";
import CalendarHeader from "@/components/calendar-header";

const meta = {
    title: "components/CalendarHeader",
    component: CalendarHeader,
} satisfies Meta<typeof CalendarHeader>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {}
};