import { defineConfig } from 'orval'

const isDev = process.env.NODE_ENV === 'development'

export default defineConfig({
    api: {
        input: {
            target: isDev
                ? 'http://localhost:9000/bikube/v3/api-docs'
                : 'target/openapi.json',
            validation: false,
        },
        output: {
            target: 'src/main/frontend/src/api/',
            client: 'react-query',
            override: {
                mutator: {
                    path: 'src/main/frontend/src/api/client.ts',
                    name: 'apiClient',
                },
            },
        },
    },
})
