import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCatalogueTitles, useCatalogueTitle } from '@/hooks/use-catalogue-title';
import {
    useSearchTitle,
    useGetSingleTitle,
} from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';

vi.mock('@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger', () => ({
    useSearchTitle: vi.fn(),
    useGetSingleTitle: vi.fn(),
    SearchTitleMaterialType: { NEWSPAPER: 'NEWSPAPER' },
    GetSingleTitleMaterialType: { NEWSPAPER: 'NEWSPAPER' },
}));

vi.mock('@/lib/utils', () => ({
    redirect: vi.fn(),
}));

const makeQueryResult = (overrides = {}) => ({
    data: undefined,
    isLoading: false,
    isError: false,
    error: null,
    refetch: vi.fn(),
    ...overrides,
});

describe('useCatalogueTitles', () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult() as any);
        vi.clearAllMocks();
        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult() as any);
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
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

    it('fetches titles when query has content', () => {
        const mockTitles = [
            { catalogueId: '1', name: 'Test Title 1' },
            { catalogueId: '2', name: 'Test Title 2' },
        ];

        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult({ data: mockTitles }) as any);

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper });

        expect(result.current.catalogueTitlesList).toEqual(mockTitles);
        expect(result.current.isLoading).toBe(false);
    });

    it('returns empty array when service returns null', () => {
        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult({ data: null }) as any);

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper });

        expect(result.current.catalogueTitlesList).toEqual([]);
    });

    it('provides refetch function', () => {
        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult({ data: [] }) as any);

        const { result } = renderHook(() => useCatalogueTitles('query'), { wrapper });

        expect(typeof result.current.search).toBe('function');
    });

    it('handles errors — returns empty list and sets isIndexUnavailable on 503', () => {
        const mockError = { response: { status: 503 } };
        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult({ isError: true, error: mockError }) as any);

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper });

        expect(result.current.catalogueTitlesList).toEqual([]);
        expect(result.current.isIndexUnavailable).toBe(true);
    });

    it('does not set isIndexUnavailable on non-503 errors', () => {
        const mockError = { response: { status: 500 } };
        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult({ isError: true, error: mockError }) as any);

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper });

        expect(result.current.isIndexUnavailable).toBe(false);
    });

    it('trims query before checking length', () => {
        vi.mocked(useSearchTitle).mockReturnValue(makeQueryResult({ isLoading: true }) as any);

        const { result: r1 } = renderHook(() => useCatalogueTitles(' '), { wrapper });
        expect(r1.current.catalogueTitlesList).toEqual([]);

        const { result: r2 } = renderHook(() => useCatalogueTitles('  test  '), { wrapper });
        expect(r2.current.isLoading).toBe(true);
    });
});

describe('useCatalogueTitle', () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
        vi.clearAllMocks();
        vi.mocked(useGetSingleTitle).mockReturnValue(makeQueryResult() as any);
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    it('fetches title by id', () => {
        const mockTitle = { catalogueId: '123', name: 'Single Test Title' };

        vi.mocked(useGetSingleTitle).mockReturnValue(makeQueryResult({ data: mockTitle }) as any);

        const { result } = renderHook(() => useCatalogueTitle('123'), { wrapper });

        expect(result.current.catalogueTitle).toEqual(mockTitle);
        expect(result.current.isLoading).toBe(false);
    });

    it('returns undefined when service returns nothing', () => {
        vi.mocked(useGetSingleTitle).mockReturnValue(makeQueryResult({ data: null }) as any);

        const { result } = renderHook(() => useCatalogueTitle('456'), { wrapper });

        expect(result.current.catalogueTitle).toBeUndefined();
    });

    it('handles errors', () => {
        vi.mocked(useGetSingleTitle).mockReturnValue(makeQueryResult({ isError: true, error: new Error('Not found') }) as any);

        const { result } = renderHook(() => useCatalogueTitle('789'), { wrapper });

        expect(result.current.catalogueTitle).toBeUndefined();
    });

    it('provides refetch function', () => {
        vi.mocked(useGetSingleTitle).mockReturnValue(makeQueryResult({ data: {} }) as any);

        const { result } = renderHook(() => useCatalogueTitle('123'), { wrapper });

        expect(typeof result.current.search).toBe('function');
    });

    it('shows loading state', () => {
        vi.mocked(useGetSingleTitle).mockReturnValue(makeQueryResult({ isLoading: true }) as any);

        const { result } = renderHook(() => useCatalogueTitle('123'), { wrapper });

        expect(result.current.isLoading).toBe(true);
    });
});
