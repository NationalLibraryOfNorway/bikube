/// <reference types="vitest" />
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCatalogueTitles, useCatalogueTitle } from '@/hooks/use-catalogue-title';
import { HuginCollectionsService } from '@/generated/endpoints';

vi.mock('@/generated/endpoints', () => ({
    HuginCollectionsService: {
        findByTitle: vi.fn(),
        findById: vi.fn()
    }
}));

vi.mock('@/lib/utils', () => ({
    redirect: vi.fn()
}));

describe('useCatalogueTitles', () => {
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

    it('returns empty array when query is empty', () => {
        const { result } = renderHook(() => useCatalogueTitles(''), { wrapper });
        
        expect(result.current.catalogueTitlesList).toEqual([]);
        expect(result.current.isLoading).toBe(false);
    });

    it('returns empty array when query is only whitespace', () => {
        const { result } = renderHook(() => useCatalogueTitles('   '), { wrapper });
        
        expect(result.current.catalogueTitlesList).toEqual([]);
        expect(result.current.isLoading).toBe(false);
    });

    it('fetches titles when query has content', async () => {
        const mockTitles = [
            { catalogueId: '1', name: 'Test Title 1' },
            { catalogueId: '2', name: 'Test Title 2' }
        ];

        vi.mocked(HuginCollectionsService.findByTitle).mockResolvedValue(mockTitles as any);

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.catalogueTitlesList).toEqual(mockTitles);
        expect(HuginCollectionsService.findByTitle).toHaveBeenCalledWith('test');
    });

    it('returns empty array when service returns null', async () => {
        vi.mocked(HuginCollectionsService.findByTitle).mockResolvedValue(null as any);

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.catalogueTitlesList).toEqual([]);
    });

    it('provides refetch function', async () => {
        vi.mocked(HuginCollectionsService.findByTitle).mockResolvedValue([]);

        const { result } = renderHook(() => useCatalogueTitles('query'), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(typeof result.current.search).toBe('function');
    });

    it('handles errors by throwing', async () => {
        const mockError = new Error('Network error');
        vi.mocked(HuginCollectionsService.findByTitle).mockRejectedValue(mockError);

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.catalogueTitlesList).toEqual([]);
    });

    it('trims query before checking length', () => {
        const { result: result1 } = renderHook(() => useCatalogueTitles(' '), { wrapper });
        expect(result1.current.catalogueTitlesList).toEqual([]);

        const { result: result2 } = renderHook(() => useCatalogueTitles('  test  '), { wrapper });
        expect(result2.current.isLoading).toBe(true);
    });
});

describe('useCatalogueTitle', () => {
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

    it('fetches title by id', async () => {
        const mockTitle = {
            catalogueId: '123',
            name: 'Single Test Title'
        };

        vi.mocked(HuginCollectionsService.findById).mockResolvedValue(mockTitle as any);

        const { result } = renderHook(() => useCatalogueTitle('123'), { wrapper });

        expect(result.current.isLoading).toBe(true);

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.catalogueTitle).toEqual(mockTitle);
        expect(HuginCollectionsService.findById).toHaveBeenCalledWith('123');
    });

    it('returns undefined when service returns nothing', async () => {
        vi.mocked(HuginCollectionsService.findById).mockResolvedValue(undefined as any);

        const { result } = renderHook(() => useCatalogueTitle('456'), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.catalogueTitle).toBeUndefined();
    });

    it('handles errors', async () => {
        const mockError = new Error('Not found');
        vi.mocked(HuginCollectionsService.findById).mockRejectedValue(mockError);

        const { result } = renderHook(() => useCatalogueTitle('789'), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.catalogueTitle).toBeUndefined();
    });

    it('provides refetch function', async () => {
        vi.mocked(HuginCollectionsService.findById).mockResolvedValue({} as any);

        const { result } = renderHook(() => useCatalogueTitle('123'), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(typeof result.current.search).toBe('function');
    });

    it('fetches with empty string id', async () => {
        vi.mocked(HuginCollectionsService.findById).mockResolvedValue(null as any);

        const { result } = renderHook(() => useCatalogueTitle(''), { wrapper });

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(HuginCollectionsService.findById).toHaveBeenCalledWith('');
    });
});

