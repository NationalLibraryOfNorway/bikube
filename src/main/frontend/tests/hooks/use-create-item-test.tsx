import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAddNewspapers } from '@/hooks/use-create-item';
import { useUpsertNewspaper } from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';
import { toast } from 'sonner';

vi.mock('@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger', () => ({
    useUpsertNewspaper: vi.fn(),
}));

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn(),
    },
}));

describe('useAddNewspapers', () => {
    let queryClient: QueryClient;
    let mockMutateAsync: ReturnType<typeof vi.fn>;

    beforeEach(() => {
        queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        vi.clearAllMocks();
        mockMutateAsync = vi.fn();
        vi.mocked(useUpsertNewspaper).mockReturnValue({
            mutateAsync: mockMutateAsync,
        } as any);
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    it('saves newspapers successfully', async () => {
        mockMutateAsync.mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 123, date: '2024-01-01', edition: 1 },
            { titleId: 123, date: '2024-01-02', edition: 2 },
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        expect(mockMutateAsync).toHaveBeenCalledWith({ data: newspapers });
    });

    it('shows success toast on successful save', async () => {
        mockMutateAsync.mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await result.current.mutateAsync({ items: [{ titleId: 456, date: '2024-01-01', edition: 1 }] as any });

        await waitFor(() => {
            expect(toast.success).toHaveBeenCalledWith('Utgaver lagret');
        });
    });

    it('invalidates query cache on success', async () => {
        mockMutateAsync.mockResolvedValue([]);

        const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await result.current.mutateAsync({ items: [{ titleId: 789, date: '2024-01-01', edition: 1 }] as any });

        await waitFor(() => {
            expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['huginTitle', 789] });
        });
    });

    it('propagates error on failure', async () => {
        mockMutateAsync.mockRejectedValue(new Error('Network error'));

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await expect(
            result.current.mutateAsync({ items: [{ titleId: 111, date: '2024-01-01', edition: 1 }] as any })
        ).rejects.toThrow('Network error');
    });

    it('handles multiple newspapers with same titleId', async () => {
        mockMutateAsync.mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        const newspapers = [
            { titleId: 222, date: '2024-01-01', edition: 1 },
            { titleId: 222, date: '2024-01-02', edition: 2 },
            { titleId: 222, date: '2024-01-03', edition: 3 },
        ];

        await result.current.mutateAsync({ items: newspapers as any });

        expect(mockMutateAsync).toHaveBeenCalledWith({ data: newspapers });

        await waitFor(() => {
            expect(toast.success).toHaveBeenCalled();
        });
    });

    it('uses titleId from first item for cache invalidation', async () => {
        mockMutateAsync.mockResolvedValue([]);

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
        mockMutateAsync.mockResolvedValue([]);

        const { result } = renderHook(() => useAddNewspapers(), { wrapper });

        await expect(
            result.current.mutateAsync({ items: [{ titleId: 555, date: '2024-01-01', edition: 1 }] as any })
        ).resolves.toBeUndefined();
    });
});
