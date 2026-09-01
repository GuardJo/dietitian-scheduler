import type {Preview} from '@storybook/nextjs-vite'
import {mswLoader} from "msw-storybook-addon/csf3";
import "../src/app/globals.css"

const preview: Preview = {
    parameters: {
        controls: {
            matchers: {
                color: /(background|color)$/i,
                date: /Date$/i,
            },
        },
        nextjs: {
            appDirectory: true,
        }
    },
    loaders: [mswLoader()]
};

export default preview;