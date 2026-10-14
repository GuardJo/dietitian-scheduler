import {Meta, StoryObj} from "@storybook/nextjs-vite";
import ScheduleRegisterModal from "@/components/schedule-register-modal";
import {action} from "storybook/actions";

const meta = {
    title: "components/ScheduleRegisterModal",
    component: ScheduleRegisterModal,
} satisfies Meta<typeof ScheduleRegisterModal>;

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