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
            shifts: {4: 'A', 5: 'C', 6: 'B', 10: 'A'},
            personalEvents: {
                5: [{id: 1, title: '치과 예약', startTime: '19:00', endTime: '20:00'}],
                12: [
                    {id: 2, title: '가족 여행', startTime: null, endTime: null},
                    {id: 3, title: '저녁 약속', startTime: '18:30', endTime: '21:00'}
                ],
                13: [{id: 4, title: '가족 여행', startTime: null, endTime: null}],
                14: [
                    {id: 5, title: '가족 여행', startTime: null, endTime: null},
                    {id: 6, title: '운동', startTime: '07:00', endTime: '08:00'}
                ],
                19: [{id: 7, title: '학회', startTime: null, endTime: null}],
                20: [{id: 8, title: '학회', startTime: null, endTime: null}]
            }
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