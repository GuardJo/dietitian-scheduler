import {Meta, StoryObj} from "@storybook/nextjs-vite";
import ShiftLegendSection from "@/components/shift-legend-section";
import {action} from "storybook/actions";

const meta = {
    title: "components/ShiftLegendSection",
    component: ShiftLegendSection,
} satisfies Meta<typeof ShiftLegendSection>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {
        colors: {
            a: '#0867c9',
            b: '#a9c4ff',
            c: '#626466'
        },
        onColorChange: action('change color')
    }
};