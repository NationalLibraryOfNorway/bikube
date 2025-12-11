import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useHuginTitle } from '@/hooks/use-hugin-title';
import { HuginNewspaperService } from '@/generated/endpoints';

vi.mock('@/generated/endpoints', () => ({
    HuginNewspaperService: {
        getTitle: vi.fn()
    }
}));

describe('useHuginTitle', () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        queryClient = new QueryClient({
            defaultOptions: {
                queries: {
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

    it('returns null when titleId is not finite', () => {
        const { result } = renderHook(() => useHuginTitle(NaN), { wrapper });

        expect(result.current.title).toBeNull();
        expect(result.current.isLoading).toBe(false);
    });

    it('fetches title data when titleId is valid', async () => {
        const mockTitle = {
            id: 123,
            name: 'Test Title',
            shelf: 'A-123'
        };

        vi.mocked(HuginNewspaperService.getTitle).mockResolvedValue(mockTitle as any);

        const { result } = renderHook(() => useHuginTitle(123), { wrapper });

        // Wait for data to be fetched
        await waitFor(() => {
            expect(result.current.title).toEqual(mockTitle);
        }, { timeout: 3000 });

        expect(HuginNewspaperService.getTitle).toHaveBeenCalledWith(123);
    });

    it('returns null when service returns null', async () => {
        vi.mocked(HuginNewspaperService.getTitle).mockResolvedValue(undefined);

        const { result } = renderHook(() => useHuginTitle(456), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.title).toBeNull();
    });

    it('handles errors correctly', async () => {
        const mockError = new Error('Failed to fetch');
        vi.mocked(HuginNewspaperService.getTitle).mockRejectedValue(mockError);

        const { result } = renderHook(() => useHuginTitle(789), { wrapper });

        await waitFor(() => {
            expect(result.current.isError).toBe(true);
        });

        expect(result.current.error).toEqual(mockError);
    });

    it('returns cached data from query client if available', async () => {
        const cachedTitle = {
            id: 999,
            name: 'Cached Title',
            shelf: 'B-999'
        };

        // Pre-populate cache
        queryClient.setQueryData(['huginTitle', 999], cachedTitle);

        const { result } = renderHook(() => useHuginTitle(999), { wrapper });

        // Should immediately have cached data
        expect(result.current.title).toEqual(cachedTitle);
    });

    it('does not fetch when titleId is Infinity', () => {
        const { result } = renderHook(() => useHuginTitle(Infinity), { wrapper });

        expect(result.current.title).toBeNull();
        expect(HuginNewspaperService.getTitle).not.toHaveBeenCalled();
    });

    it('does not fetch when titleId is negative infinity', () => {
        const { result } = renderHook(() => useHuginTitle(-Infinity), { wrapper });

        expect(result.current.title).toBeNull();
        expect(HuginNewspaperService.getTitle).not.toHaveBeenCalled();
    });

    it('fetches when titleId is zero', async () => {
        const mockTitle = { id: 0, name: 'Zero Title' };
        vi.mocked(HuginNewspaperService.getTitle).mockResolvedValue(mockTitle as any);

        const { result } = renderHook(() => useHuginTitle(0), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(HuginNewspaperService.getTitle).toHaveBeenCalledWith(0);
    });

    it('fetches when titleId is negative number', async () => {
        const mockTitle = { id: -5, name: 'Negative Title' };
        vi.mocked(HuginNewspaperService.getTitle).mockResolvedValue(mockTitle as any);

        const { result } = renderHook(() => useHuginTitle(-5), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(HuginNewspaperService.getTitle).toHaveBeenCalledWith(-5);
    });
});

