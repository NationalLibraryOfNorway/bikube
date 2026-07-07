import { defineConfig } from 'orval'

const target = process.env.ORVAL_TARGET ?? 'openapi.json'

export default defineConfig({
    api: {
        input: {
            target,
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
