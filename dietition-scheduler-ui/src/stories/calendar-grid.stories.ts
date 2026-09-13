import {Meta, StoryObj} from "@storybook/nextjs-vite";
import CalendarGrid from "@/components/calendar-grid";

const meta = {
    title: "components/CalendarGrid",
    component: CalendarGrid,
} satisfies Meta<typeof CalendarGrid>;

export default meta;

type Story = StoryObj<typeof meta>;

export const HasData: Story = {
    args: {
        monthData: {
            year: 2026,
            month: 9,
            label: 'September 2026',
            shiftCount: 22,
            shifts: {4: 'a', 5: 'c', 6: 'b', 10: 'a'}
        },
        colors: {
            a: '#0867c9',
            b: '#a9c4ff',
            c: '#626466'
        },
    }
};

export const Empty: Story = {
    args: {
        monthData: {
            year: 2026,
            month: 10,
            label: 'October 2026',
            shiftCount: 0,
            shifts: {}
        },
        colors: {
            a: '#0867c9',
            b: '#a9c4ff',
            c: '#626466'
        },
    }
};