import {Meta, StoryObj} from "@storybook/nextjs-vite";
import InfoSection from "@/components/info-section";

const meta = {
    title: "components/InfoSection",
    component: InfoSection,
} satisfies Meta<typeof InfoSection>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {
        children: <h1>Test</h1>
    }
}