import type {Preview} from '@storybook/nextjs-vite'
import {mswLoader} from "msw-storybook-addon/csf3";
import "../src/app/globals.css"
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {handlers} from "../src/mocks/handlers";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";

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
        },
        msw: {handlers}
    },
    loaders: [mswLoader()],
    decorators: [
        (Story) => (
            <QueryClientProvider client={new QueryClient({defaultOptions: {mutations: {retry: 0}}})}>
                <Story/>
                <ReactQueryDevtools initialIsOpen={false}/>
            </QueryClientProvider>
        ),
    ]
};

export default preview;