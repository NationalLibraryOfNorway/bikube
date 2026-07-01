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
        port: 5173,
        proxy: {
            '/bikube/api':    'http://localhost:8080',
            '/bikube/oauth2': 'http://localhost:8080',
            '/bikube/login':  'http://localhost:8080',
            '/bikube/logout': 'http://localhost:8080',
        },
    },
})
