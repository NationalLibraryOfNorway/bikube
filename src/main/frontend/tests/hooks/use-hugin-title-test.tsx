import 'vitest';
import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import { useHuginTitle } from '@/hooks/use-hugin-title';
import { server } from '../setup/server';

const makeWrapper = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } },
    });
    return ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
};

describe('useHuginTitle', () => {
    it('returns null and does not fetch when titleId is NaN', () => {
        const { result } = renderHook(() => useHuginTitle(NaN), { wrapper: makeWrapper() });
        expect(result.current.title).toBeNull();
        expect(result.current.isLoading).toBe(false);
    });

    it('returns null and does not fetch when titleId is Infinity', () => {
        const { result } = renderHook(() => useHuginTitle(Infinity), { wrapper: makeWrapper() });
        expect(result.current.title).toBeNull();
        expect(result.current.isLoading).toBe(false);
    });

    it('returns null and does not fetch when titleId is -Infinity', () => {
        const { result } = renderHook(() => useHuginTitle(-Infinity), { wrapper: makeWrapper() });
        expect(result.current.title).toBeNull();
        expect(result.current.isLoading).toBe(false);
    });

    it('fetches and returns title when titleId is valid', async () => {
        const mockTitle = { id: 123, name: 'Test Title', shelf: 'A-123' };

        server.use(
            http.get('*/api/hugin/titles/:id', () => HttpResponse.json(mockTitle))
        );

        const { result } = renderHook(() => useHuginTitle(123), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.title).toEqual(mockTitle);
    });

    it('returns null when server returns null', async () => {
        server.use(
            http.get('*/api/hugin/titles/:id', () => HttpResponse.json(null))
        );

        const { result } = renderHook(() => useHuginTitle(456), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.title).toBeNull();
    });

    it('exposes isError and error on failure', async () => {
        server.use(
            http.get('*/api/hugin/titles/:id', () => new HttpResponse(null, { status: 500 }))
        );

        const { result } = renderHook(() => useHuginTitle(789), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isError).toBe(true));
        expect(result.current.error).toBeTruthy();
    });

    it('returns cached data from query client without fetching', async () => {
        const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
        const cachedTitle = { id: 999, name: 'Cached Title', shelf: 'B-999' };
        queryClient.setQueryData(['huginTitle', 999], cachedTitle);

        const wrapper = ({ children }: { children: React.ReactNode }) => (
            <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
        );

        const { result } = renderHook(() => useHuginTitle(999), { wrapper });
        expect(result.current.title).toEqual(cachedTitle);
    });

    it('fetches when titleId is zero', async () => {
        const mockTitle = { id: 0, name: 'Zero Title' };
        server.use(
            http.get('*/api/hugin/titles/:id', () => HttpResponse.json(mockTitle))
        );

        const { result } = renderHook(() => useHuginTitle(0), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.title).toEqual(mockTitle);
    });

    it('fetches when titleId is a negative number', async () => {
        const mockTitle = { id: -5, name: 'Negative Title' };
        server.use(
            http.get('*/api/hugin/titles/:id', () => HttpResponse.json(mockTitle))
        );

        const { result } = renderHook(() => useHuginTitle(-5), { wrapper: makeWrapper() });

        await waitFor(() => expect(result.current.isLoading).toBe(false));
        expect(result.current.title).toEqual(mockTitle);
    });
});
