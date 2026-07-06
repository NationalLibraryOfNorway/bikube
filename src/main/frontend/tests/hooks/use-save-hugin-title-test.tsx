import 'vitest';
import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import { useSaveHuginTitle } from '@/hooks/use-save-hugin-title';
import { server } from '../setup/server';

const makeWrapper = (queryClient: QueryClient) =>
    ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

describe('useSaveHuginTitle', () => {
    it('sends normalized payload to the API', async () => {
        let capturedBody: unknown;

        server.use(
            http.put('*/api/hugin/titles/contact', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ id: 123, vendor: 'Test Vendor' });
            })
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes',
            releasePattern: [1, 2, 3, 4, 5, 6, 7],
        });

        expect(capturedBody).toEqual({
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes',
            contactInfos: undefined,
            releasePattern: [1, 2, 3, 4, 5, 6, 7],
        });
    });

    it('trims whitespace from string fields before sending', async () => {
        let capturedBody: unknown;

        server.use(
            http.put('*/api/hugin/titles/contact', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ id: 123 });
            })
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({
            id: 123,
            vendor: '  Test Vendor  ',
            contactName: '  John Doe  ',
            shelf: '  A-123  ',
            notes: '  Test notes  ',
        });

        expect(capturedBody).toEqual({
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes',
            contactInfos: undefined,
            releasePattern: undefined,
        });
    });

    it('converts empty strings to undefined', async () => {
        let capturedBody: unknown;

        server.use(
            http.put('*/api/hugin/titles/contact', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ id: 456 });
            })
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({ id: 456, vendor: '', contactName: '   ', shelf: '', notes: '' });

        expect(capturedBody).toEqual({
            id: 456,
            vendor: undefined,
            contactName: undefined,
            shelf: undefined,
            notes: undefined,
            contactInfos: undefined,
            releasePattern: undefined,
        });
    });

    it('filters out empty contact infos before sending', async () => {
        let capturedBody: any;

        server.use(
            http.put('*/api/hugin/titles/contact', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ id: 789 });
            })
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({
            id: 789,
            contactInfos: [
                { contactType: 'PHONE' as any, contactValue: '12345678' },
                { contactType: null as any, contactValue: '98765432' },
                { contactType: 'EMAIL' as any, contactValue: '' },
                { contactType: 'EMAIL' as any, contactValue: '   ' },
            ],
        });

        expect(capturedBody.contactInfos).toEqual([{ contactType: 'PHONE', contactValue: '12345678' }]);
    });

    it('trims contact info values', async () => {
        let capturedBody: any;

        server.use(
            http.put('*/api/hugin/titles/contact', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ id: 111 });
            })
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({
            id: 111,
            contactInfos: [{ contactType: 'PHONE' as any, contactValue: '  12345678  ' }],
        });

        expect(capturedBody.contactInfos).toEqual([{ contactType: 'PHONE', contactValue: '12345678' }]);
    });

    it('truncates release pattern numbers to integers', async () => {
        let capturedBody: any;

        server.use(
            http.put('*/api/hugin/titles/contact', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ id: 222 });
            })
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({ id: 222, releasePattern: [1.7, 2.3, 3.9, 4.1, 5.5, 6.6, 7.2] });

        expect(capturedBody.releasePattern).toEqual([1, 2, 3, 4, 5, 6, 7]);
    });

    it('updates query cache with the saved title', async () => {
        const saved = { id: 333, vendor: 'Updated Vendor' };

        server.use(
            http.put('*/api/hugin/titles/contact', async () => HttpResponse.json(saved))
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({ id: 333, vendor: 'Updated Vendor' });

        await waitFor(() => {
            expect(queryClient.getQueryData(['huginTitle', 333])).toEqual(saved);
        });
    });

    it('rejects when the server returns an error', async () => {
        server.use(
            http.put('*/api/hugin/titles/contact', () => new HttpResponse(null, { status: 500 }))
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await expect(result.current.mutateAsync({ id: 444 })).rejects.toThrow();
    });

    it('converts null string fields to undefined', async () => {
        let capturedBody: unknown;

        server.use(
            http.put('*/api/hugin/titles/contact', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ id: 555 });
            })
        );

        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper: makeWrapper(queryClient) });

        await result.current.mutateAsync({ id: 555, vendor: null as any, contactName: null as any });

        expect(capturedBody).toEqual({
            id: 555,
            vendor: undefined,
            contactName: undefined,
            shelf: undefined,
            notes: undefined,
            contactInfos: undefined,
            releasePattern: undefined,
        });
    });
});
