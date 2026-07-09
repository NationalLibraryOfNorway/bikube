import path from 'path'
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src/main/frontend'),
        },
    },
    test: {
        environment: 'jsdom',
        globals: true,
        setupFiles: ['src/main/frontend/tests/setup/setup.ts'],
        include: ['src/main/frontend/tests/**/*-test.{ts,tsx}'],
    },
})
