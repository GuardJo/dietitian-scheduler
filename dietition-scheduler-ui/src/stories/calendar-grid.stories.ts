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
            shifts: {4: 'A', 5: 'C', 6: 'B', 10: 'A'}
        },
        colors: {
            A: '#0867c9',
            B: '#a9c4ff',
            C: '#626466',
            ALL: '#10B981'
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
            A: '#0867c9',
            B: '#a9c4ff',
            C: '#626466',
            ALL: '#10B981'
        },
    }
};