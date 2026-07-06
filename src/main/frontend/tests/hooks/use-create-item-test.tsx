import 'vitest';
import { describe, it, expect, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import { useAddNewspapers } from '@/hooks/use-create-item';
import { server } from '../setup/server';

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn(),
    },
}));

import { toast } from 'sonner';

const makeWrapper = () => {
    const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
    return {
        queryClient,
        wrapper: ({ children }: { children: React.ReactNode }) => (
            <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
        ),
    };
};

describe('useAddNewspapers', () => {
    it('sends newspapers to the batch endpoint', async () => {
        let capturedBody: unknown;

        server.use(
            http.post('*/api/hugin/newspapers/batch', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json([]);
            })
        );

        const { wrapper } = makeWrapper();
        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 123, date: '2024-01-01', edition: 1 },
            { titleId: 123, date: '2024-01-02', edition: 2 },
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        expect(capturedBody).toEqual(newspapers);
    });

    it('shows success toast on successful save', async () => {
        server.use(
            http.post('*/api/hugin/newspapers/batch', () => HttpResponse.json([]))
        );

        const { wrapper } = makeWrapper();
        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await result.current.mutateAsync({ items: [{ titleId: 456, date: '2024-01-01', edition: 1 }] as any });

        await waitFor(() => {
            expect(toast.success).toHaveBeenCalledWith('Utgaver lagret');
        });
    });

    it('invalidates query cache on success', async () => {
        server.use(
            http.post('*/api/hugin/newspapers/batch', () => HttpResponse.json([]))
        );

        const { queryClient, wrapper } = makeWrapper();
        const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await result.current.mutateAsync({ items: [{ titleId: 789, date: '2024-01-01', edition: 1 }] as any });

        await waitFor(() => {
            expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['huginTitle', 789] });
        });
    });

    it('propagates error on failure', async () => {
        server.use(
            http.post('*/api/hugin/newspapers/batch', () => new HttpResponse(null, { status: 500 }))
        );

        const { wrapper } = makeWrapper();
        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await expect(
            result.current.mutateAsync({ items: [{ titleId: 111, date: '2024-01-01', edition: 1 }] as any })
        ).rejects.toThrow();
    });

    it('handles multiple newspapers with same titleId', async () => {
        server.use(
            http.post('*/api/hugin/newspapers/batch', () => HttpResponse.json([]))
        );

        const { wrapper } = makeWrapper();
        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 222, date: '2024-01-01', edition: 1 },
            { titleId: 222, date: '2024-01-02', edition: 2 },
            { titleId: 222, date: '2024-01-03', edition: 3 },
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        await waitFor(() => {
            expect(toast.success).toHaveBeenCalled();
        });
    });

    it('uses titleId from first item for cache invalidation', async () => {
        server.use(
            http.post('*/api/hugin/newspapers/batch', () => HttpResponse.json([]))
        );

        const { queryClient, wrapper } = makeWrapper();
        const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 333, date: '2024-01-01', edition: 1 },
            { titleId: 444, date: '2024-01-02', edition: 2 },
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        await waitFor(() => {
            expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['huginTitle', 333] });
        });
    });

    it('resolves to undefined on success', async () => {
        server.use(
            http.post('*/api/hugin/newspapers/batch', () => HttpResponse.json([]))
        );

        const { wrapper } = makeWrapper();
        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await expect(
            result.current.mutateAsync({ items: [{ titleId: 555, date: '2024-01-01', edition: 1 }] as any })
        ).resolves.toBeUndefined();
    });
});
