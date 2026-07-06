import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useHuginTitle } from '@/hooks/use-hugin-title';
import { useGetTitle } from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';

vi.mock('@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger', () => ({
    useGetTitle: vi.fn(),
}));

const makeQueryResult = (overrides = {}) => ({
    data: undefined,
    isLoading: false,
    isError: false,
    error: null,
    ...overrides,
});

describe('useHuginTitle', () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
        vi.clearAllMocks();
        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult() as any);
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    it('returns null when titleId is not finite (NaN)', () => {
        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: undefined }) as any);

        const { result } = renderHook(() => useHuginTitle(NaN), { wrapper });

        expect(result.current.title).toBeNull();
        expect(result.current.isLoading).toBe(false);
    });

    it('fetches title data when titleId is valid', () => {
        const mockTitle = { id: 123, name: 'Test Title', shelf: 'A-123' };

        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: mockTitle }) as any);

        const { result } = renderHook(() => useHuginTitle(123), { wrapper });

        expect(result.current.title).toEqual(mockTitle);
        expect(result.current.isLoading).toBe(false);
    });

    it('returns null when service returns null', () => {
        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: null }) as any);

        const { result } = renderHook(() => useHuginTitle(456), { wrapper });

        expect(result.current.title).toBeNull();
    });

    it('handles errors correctly', () => {
        const mockError = new Error('Failed to fetch');

        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ isError: true, error: mockError }) as any);

        const { result } = renderHook(() => useHuginTitle(789), { wrapper });

        expect(result.current.isError).toBe(true);
        expect(result.current.error).toEqual(mockError);
    });

    it('returns cached data from query client if available', () => {
        const cachedTitle = { id: 999, name: 'Cached Title', shelf: 'B-999' };

        queryClient.setQueryData(['huginTitle', 999], cachedTitle);

        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: cachedTitle }) as any);

        const { result } = renderHook(() => useHuginTitle(999), { wrapper });

        expect(result.current.title).toEqual(cachedTitle);
    });

    it('does not fetch when titleId is Infinity — returns null', () => {
        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: undefined, isLoading: false }) as any);

        const { result } = renderHook(() => useHuginTitle(Infinity), { wrapper });

        expect(result.current.title).toBeNull();
        expect(result.current.isLoading).toBe(false);
    });

    it('does not fetch when titleId is negative infinity — returns null', () => {
        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: undefined, isLoading: false }) as any);

        const { result } = renderHook(() => useHuginTitle(-Infinity), { wrapper });

        expect(result.current.title).toBeNull();
        expect(result.current.isLoading).toBe(false);
    });

    it('fetches when titleId is zero', () => {
        const mockTitle = { id: 0, name: 'Zero Title' };

        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: mockTitle }) as any);

        const { result } = renderHook(() => useHuginTitle(0), { wrapper });

        expect(result.current.title).toEqual(mockTitle);
    });

    it('fetches when titleId is negative number', () => {
        const mockTitle = { id: -5, name: 'Negative Title' };

        vi.mocked(useGetTitle).mockReturnValue(makeQueryResult({ data: mockTitle }) as any);

        const { result } = renderHook(() => useHuginTitle(-5), { wrapper });

        expect(result.current.title).toEqual(mockTitle);
    });
});
