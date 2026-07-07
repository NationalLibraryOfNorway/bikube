import { defineConfig } from 'orval'

export default defineConfig({
    api: {
        input: {
            target: 'openapi.json',
            validation: false,
        },
        output: {
            target: 'src/main/frontend/src/api/',
            client: 'react-query',
            mock: {
                type: 'msw',
                delay: 0,
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
