import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useSaveHuginTitle } from '@/hooks/use-save-hugin-title';
import { HuginNewspaperService } from '@/generated/endpoints';

vi.mock('@/generated/endpoints', () => ({
    HuginNewspaperService: {
        upsertContactInformation: vi.fn()
    }
}));

describe('useSaveHuginTitle', () => {
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

    it('saves title with all fields', async () => {
        const mockSavedTitle = {
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes'
        };

        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue(mockSavedTitle as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        const payload = {
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes',
            releasePattern: [1, 2, 3, 4, 5, 6, 7]
        };

        await result.current.mutateAsync(payload);

        expect(HuginNewspaperService.upsertContactInformation).toHaveBeenCalledWith({
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes',
            contactInfos: undefined,
            releasePattern: [1, 2, 3, 4, 5, 6, 7]
        });
    });

    it('trims whitespace from string fields', async () => {
        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue({ id: 123 } as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        const payload = {
            id: 123,
            vendor: '  Test Vendor  ',
            contactName: '  John Doe  ',
            shelf: '  A-123  ',
            notes: '  Test notes  '
        };

        await result.current.mutateAsync(payload);

        expect(HuginNewspaperService.upsertContactInformation).toHaveBeenCalledWith({
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes',
            contactInfos: undefined,
            releasePattern: undefined
        });
    });

    it('converts empty strings to undefined', async () => {
        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue({ id: 456 } as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        const payload = {
            id: 456,
            vendor: '',
            contactName: '   ',
            shelf: '',
            notes: ''
        };

        await result.current.mutateAsync(payload);

        expect(HuginNewspaperService.upsertContactInformation).toHaveBeenCalledWith({
            id: 456,
            vendor: undefined,
            contactName: undefined,
            shelf: undefined,
            notes: undefined,
            contactInfos: undefined,
            releasePattern: undefined
        });
    });

    it('filters out empty contact infos', async () => {
        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue({ id: 789 } as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        const payload = {
            id: 789,
            contactInfos: [
                { contactType: 'PHONE' as any, contactValue: '12345678' },
                { contactType: null as any, contactValue: '98765432' },
                { contactType: 'EMAIL' as any, contactValue: '' },
                { contactType: 'EMAIL' as any, contactValue: '   ' }
            ]
        };

        await result.current.mutateAsync(payload);

        const call = vi.mocked(HuginNewspaperService.upsertContactInformation).mock.calls[0][0];
        expect(call.contactInfos).toEqual([
            { contactType: 'PHONE', contactValue: '12345678' }
        ]);
    });

    it('trims contact info values', async () => {
        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue({ id: 111 } as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        const payload = {
            id: 111,
            contactInfos: [
                { contactType: 'PHONE' as any, contactValue: '  12345678  ' }
            ]
        };

        await result.current.mutateAsync(payload);

        const call = vi.mocked(HuginNewspaperService.upsertContactInformation).mock.calls[0][0];
        expect(call.contactInfos).toEqual([
            { contactType: 'PHONE', contactValue: '12345678' }
        ]);
    });

    it('truncates release pattern numbers', async () => {
        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue({ id: 222 } as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        const payload = {
            id: 222,
            releasePattern: [1.7, 2.3, 3.9, 4.1, 5.5, 6.6, 7.2]
        };

        await result.current.mutateAsync(payload);

        const call = vi.mocked(HuginNewspaperService.upsertContactInformation).mock.calls[0][0];
        expect(call.releasePattern).toEqual([1, 2, 3, 4, 5, 6, 7]);
    });

    it('updates query cache on success', async () => {
        const mockSavedTitle = {
            id: 333,
            vendor: 'Updated Vendor'
        };

        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue(mockSavedTitle as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({ id: 333, vendor: 'Updated Vendor' });

        await waitFor(() => {
            const cachedData = queryClient.getQueryData(['huginTitle', 333]);
            expect(cachedData).toEqual(mockSavedTitle);
        });
    });

    it('handles save errors', async () => {
        const mockError = new Error('Save failed');
        vi.mocked(HuginNewspaperService.upsertContactInformation).mockRejectedValue(mockError);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await expect(result.current.mutateAsync({ id: 444 })).rejects.toThrow('Save failed');
    });

    it('handles null values in payload', async () => {
        vi.mocked(HuginNewspaperService.upsertContactInformation).mockResolvedValue({ id: 555 } as any);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        const payload = {
            id: 555,
            vendor: null as any,
            contactName: null as any
        };

        await result.current.mutateAsync(payload);

        expect(HuginNewspaperService.upsertContactInformation).toHaveBeenCalledWith({
            id: 555,
            vendor: undefined,
            contactName: undefined,
            shelf: undefined,
            notes: undefined,
            contactInfos: undefined,
            releasePattern: undefined
        });
    });
});

