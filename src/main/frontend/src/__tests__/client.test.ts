import { describe, it, expect, vi } from 'vitest'

vi.mock('axios', () => ({
    default: {
        create: vi.fn(() => ({
            interceptors: {
                response: { use: vi.fn() },
            },
        })),
    },
}))

describe('axios client', () => {
    it('module exports a default axios instance', async () => {
        const { default: client } = await import('../api/client')
        expect(client).toBeDefined()
    })
})
