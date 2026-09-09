import {Meta, StoryObj} from "@storybook/nextjs-vite";
import NavigationBar from "@/components/navigation-bar";

const meta = {
    title: "components/NavigationBar",
    component: NavigationBar,
} satisfies Meta<typeof NavigationBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {}
}