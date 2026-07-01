import { defineConfig } from 'orval'

const isDev = process.env.NODE_ENV === 'development'

export default defineConfig({
    api: {
        input: isDev
            ? 'http://localhost:8080/bikube/v3/api-docs'
            : 'target/openapi.json',
        output: {
            target: 'src/main/frontend/src/api/',
            client: 'react-query',
            schemas: {
                path: 'src/main/frontend/src/api/model',
                type: 'zod',
            },
            override: {
                mutator: {
                    path: 'src/main/frontend/src/api/client.ts',
                    name: 'apiClient',
                },
            },
        },
    },
})
