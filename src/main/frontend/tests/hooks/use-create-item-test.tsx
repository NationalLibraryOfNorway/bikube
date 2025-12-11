import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAddNewspapers } from '@/hooks/use-create-item';
import { HuginNewspaperService } from '@/generated/endpoints';
import { toast } from 'sonner';

vi.mock('@/generated/endpoints', () => ({
    HuginNewspaperService: {
        upsertNewspaper: vi.fn()
    }
}));

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn()
    }
}));

describe('useAddNewspapers', () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        queryClient = new QueryClient({
            defaultOptions: {
                mutations: {
                    retry: false,
                },
            },
        });
        vi.clearAllMocks();
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>
            {children}
        </QueryClientProvider>
    );

    it('saves newspapers successfully', async () => {
        vi.mocked(HuginNewspaperService.upsertNewspaper).mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 123, date: '2024-01-01', edition: 1 },
            { titleId: 123, date: '2024-01-02', edition: 2 }
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        expect(HuginNewspaperService.upsertNewspaper).toHaveBeenCalledWith(newspapers);
    });

    it('shows success toast on successful save', async () => {
        vi.mocked(HuginNewspaperService.upsertNewspaper).mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 456, date: '2024-01-01', edition: 1 }
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        await waitFor(() => {
            expect(toast.success).toHaveBeenCalledWith('Utgaver lagret');
        });
    });

    it('invalidates query cache on success', async () => {
        vi.mocked(HuginNewspaperService.upsertNewspaper).mockResolvedValue([]);

        const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 789, date: '2024-01-01', edition: 1 }
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        await waitFor(() => {
            expect(invalidateSpy).toHaveBeenCalledWith({
                queryKey: ['huginTitle', 789]
            });
        });
    });

    it('shows error toast on failure', async () => {
        const mockError = new Error('Network error');
        vi.mocked(HuginNewspaperService.upsertNewspaper).mockRejectedValue(mockError);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 111, date: '2024-01-01', edition: 1 }
        ];

        await expect(result.current.mutateAsync({ items: newspapers as any })).rejects.toThrow();

        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Klarte ikke å lagre utgaver');
        });
    });


    it('handles multiple newspapers with same titleId', async () => {
        vi.mocked(HuginNewspaperService.upsertNewspaper).mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 222, date: '2024-01-01', edition: 1 },
            { titleId: 222, date: '2024-01-02', edition: 2 },
            { titleId: 222, date: '2024-01-03', edition: 3 }
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        expect(HuginNewspaperService.upsertNewspaper).toHaveBeenCalledWith(newspapers);

        await waitFor(() => {
            expect(toast.success).toHaveBeenCalled();
        });
    });

    it('uses titleId from first item for cache invalidation', async () => {
        vi.mocked(HuginNewspaperService.upsertNewspaper).mockResolvedValue([]);

        const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 333, date: '2024-01-01', edition: 1 },
            { titleId: 444, date: '2024-01-02', edition: 2 } // Different titleId
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        await waitFor(() => {
            // Should use titleId from first item
            expect(invalidateSpy).toHaveBeenCalledWith({
                queryKey: ['huginTitle', 333]
            });
        });
    });

    it('handles service returning void', async () => {
        vi.mocked(HuginNewspaperService.upsertNewspaper).mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 555, date: '2024-01-01', edition: 1 }
        ];

        await expect(
            result.current.mutateAsync({ items: newspapers as any })
        ).resolves.toBeUndefined();
    });
});

