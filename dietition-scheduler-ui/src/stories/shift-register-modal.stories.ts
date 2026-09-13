import {Meta, StoryObj} from "@storybook/nextjs-vite";
import ShiftRegisterModal from "@/components/shift-register-modal";
import {action} from "storybook/actions";

const meta = {
    title: "components/ShiftRegisterModal",
    component: ShiftRegisterModal,
} satisfies Meta<typeof ShiftRegisterModal>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {
        baseYear: 2026,
        baseMonth: 9,
        isOpen: true,
        setIsOpen: action('Change isOpen')
    }
};