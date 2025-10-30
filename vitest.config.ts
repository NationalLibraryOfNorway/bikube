import type { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';
import path from "path";

const customConfig: UserConfigFn = (env) => ({
    root: path.resolve(__dirname, "src/main/frontend"),
    cacheDir: path.resolve(__dirname, "node_modules/.vite-vitest"),
    resolve: {
        alias: [
            { find: "@", replacement: path.resolve(__dirname, "src/main/frontend") },
        ],
    },
    optimizeDeps: {
        include: [
            'react',
            'react-dom',
            'react-router-dom',
            'react/jsx-runtime',
            '@testing-library/react',
            '@preact/signals-react',
            '@preact/signals-react/runtime'
        ],
        force: true,
    },
    test: {
        include: [
            './tests/**/*.{test,spec}.ts?(x)',
            './tests/**/*-{test,spec}.ts?(x)'
        ],
        globals: true,
        setupFiles: ['./tests/setup/setup.ts'],
        browser: {
            enabled: true,
            provider: 'playwright',
            instances: [
                { browser: 'chromium', headless: true },
            ]
        },
    },
});

export default overrideVaadinConfig(customConfig);
