import type { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';
import path from "path";

const customConfig: UserConfigFn = (env) => ({
    root: path.resolve(__dirname, "src/main/frontend"),
    cacheDir: path.resolve(__dirname, "node_modules/.vite-vitest"),
    resolve: {
        alias: [
            { find: "@", replacement: path.resolve(__dirname, "src/main/frontend") },
            {
                find: '@vaadin/hilla-react-i18n',
                replacement: path.resolve(__dirname, 'src/main/frontend/tests/shims/hilla-react-i18n.tsx')
            },
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
        include: ['./tests/**/*.{test,spec}.ts?(x)'],
        globals: true,
        browser: {
            enabled: true,
            provider: 'playwright',
            instances: [
                { browser: 'chromium', headless: true },
            ]
        },
        setupFiles: [ './tests/setup/setup.tsx' ],
    },
});

export default overrideVaadinConfig(customConfig);
