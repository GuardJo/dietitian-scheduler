import {Meta, StoryObj} from "@storybook/nextjs-vite";
import CalendarHeader from "@/components/calendar-header";
import {action} from "storybook/actions";

const meta = {
    title: "components/CalendarHeader",
    component: CalendarHeader,
} satisfies Meta<typeof CalendarHeader>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {
        selectedYear: 2026,
        selectedMonthNumber: 10,
        onYearChange: action('Change year'),
        onMonthChange: action('Change month')
    }
};