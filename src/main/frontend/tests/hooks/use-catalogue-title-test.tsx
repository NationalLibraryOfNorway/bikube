import 'vitest';
import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import { useCatalogueTitles, useCatalogueTitle } from '@/hooks/use-catalogue-title';
import { server } from '../setup/server';

const makeWrapper = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } },
    });
    return ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
};

describe('useCatalogueTitles', () => {
    it('returns empty array when query is empty — no request made', () => {
        const { result } = renderHook(() => useCatalogueTitles(''), { wrapper: makeWrapper() });
        expect(result.current.catalogueTitlesList).toEqual([]);
        expect(result.current.isLoading).toBe(false);
    });

    it('returns empty array when query is only whitespace — no request made', () => {
        const { result } = renderHook(() => useCatalogueTitles('   '), { wrapper: makeWrapper() });
        expect(result.current.catalogueTitlesList).toEqual([]);
        expect(result.current.isLoading).toBe(false);
    });

    it('fetches and returns titles when query has content', async () => {
        const mockTitles = [
            { catalogueId: '1', name: 'Test Title 1' },
            { catalogueId: '2', name: 'Test Title 2' },
        ];

        server.use(
            http.get('*/api/title/search', () => HttpResponse.json(mockTitles))
        );

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.catalogueTitlesList).toEqual(mockTitles);
    });

    it('returns empty array when server returns null', async () => {
        server.use(
            http.get('*/api/title/search', () => HttpResponse.json(null))
        );

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.catalogueTitlesList).toEqual([]);
    });

    it('provides a refetch function', async () => {
        server.use(
            http.get('*/api/title/search', () => HttpResponse.json([]))
        );

        const { result } = renderHook(() => useCatalogueTitles('query'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(typeof result.current.search).toBe('function');
    });

    it('sets isIndexUnavailable when server returns 503', async () => {
        server.use(
            http.get('*/api/title/search', () => new HttpResponse(null, { status: 503 }))
        );

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.isIndexUnavailable).toBe(true);
        expect(result.current.catalogueTitlesList).toEqual([]);
    });

    it('does not set isIndexUnavailable on non-503 errors', async () => {
        server.use(
            http.get('*/api/title/search', () => new HttpResponse(null, { status: 500 }))
        );

        const { result } = renderHook(() => useCatalogueTitles('test'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.isIndexUnavailable).toBe(false);
    });

    it('enables fetch only when trimmed query is non-empty', async () => {
        server.use(
            http.get('*/api/title/search', () => HttpResponse.json([{ catalogueId: '1', name: 'X' }]))
        );

        const { result: empty } = renderHook(() => useCatalogueTitles(' '), { wrapper: makeWrapper() });
        expect(empty.current.catalogueTitlesList).toEqual([]);

        const { result: nonEmpty } = renderHook(() => useCatalogueTitles('  test  '), { wrapper: makeWrapper() });
        await waitFor(() => expect(nonEmpty.current.isLoading).toBe(false));
        expect(nonEmpty.current.catalogueTitlesList).toHaveLength(1);
    });
});

describe('useCatalogueTitle', () => {
    it('fetches and returns title by id', async () => {
        const mockTitle = { catalogueId: '123', name: 'Single Test Title' };

        server.use(
            http.get('*/api/title', () => HttpResponse.json(mockTitle))
        );

        const { result } = renderHook(() => useCatalogueTitle('123'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.catalogueTitle).toEqual(mockTitle);
    });

    it('returns undefined when server returns null', async () => {
        server.use(
            http.get('*/api/title', () => HttpResponse.json(null))
        );

        const { result } = renderHook(() => useCatalogueTitle('456'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.catalogueTitle).toBeUndefined();
    });

    it('returns undefined on error', async () => {
        server.use(
            http.get('*/api/title', () => new HttpResponse(null, { status: 404 }))
        );

        const { result } = renderHook(() => useCatalogueTitle('789'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.catalogueTitle).toBeUndefined();
    });

    it('provides a refetch function', async () => {
        server.use(
            http.get('*/api/title', () => HttpResponse.json({ catalogueId: '1', name: 'X' }))
        );

        const { result } = renderHook(() => useCatalogueTitle('123'), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(typeof result.current.search).toBe('function');
    });
});
