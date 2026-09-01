import LoginSection from "@/components/login-section";
import type {Meta, StoryObj} from "@storybook/nextjs-vite"

const meta = {
    title: "components/LoginSection",
    component: LoginSection,
} satisfies Meta<typeof LoginSection>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {}
};