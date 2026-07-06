import path from 'path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
    root: path.resolve(__dirname, 'src/main/frontend'),
    base: '/bikube/hugin/',
    build: {
        outDir: path.resolve(__dirname, 'src/main/resources/static/hugin'),
        emptyOutDir: true,
    },
    plugins: [
        react(),
        tailwindcss(),
    ],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src/main/frontend'),
        },
    },
    server: {
        port: 8087,
        proxy: {
            '/bikube/api': { target: 'http://localhost:9000' },
            '/bikube/oauth2': {
                target: 'http://localhost:9000',
                headers: { 'X-Forwarded-Host': 'localhost:8087', 'X-Forwarded-Proto': 'http' },
            },
            '/bikube/login': {
                target: 'http://localhost:9000',
                headers: { 'X-Forwarded-Host': 'localhost:8087', 'X-Forwarded-Proto': 'http' },
            },
            '/bikube/logout': {
                target: 'http://localhost:9000',
                headers: { 'X-Forwarded-Host': 'localhost:8087', 'X-Forwarded-Proto': 'http' },
            },
        },
    },
})
