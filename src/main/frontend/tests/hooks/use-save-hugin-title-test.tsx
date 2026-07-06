import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useSaveHuginTitle } from '@/hooks/use-save-hugin-title';
import { useUpsertContactInformation } from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';

vi.mock('@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger', () => ({
    useUpsertContactInformation: vi.fn(),
}));

describe('useSaveHuginTitle', () => {
    let queryClient: QueryClient;
    let mockMutateAsync: ReturnType<typeof vi.fn>;

    beforeEach(() => {
        queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        mockMutateAsync = vi.fn();
        vi.mocked(useUpsertContactInformation).mockReturnValue({
            mutateAsync: mockMutateAsync,
        } as any);
        vi.clearAllMocks();
        mockMutateAsync = vi.fn();
        vi.mocked(useUpsertContactInformation).mockReturnValue({
            mutateAsync: mockMutateAsync,
        } as any);
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    it('saves title with all fields', async () => {
        const mockSavedTitle = { id: 123, vendor: 'Test Vendor', contactName: 'John Doe', shelf: 'A-123', notes: 'Test notes' };
        mockMutateAsync.mockResolvedValue(mockSavedTitle);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({
            id: 123,
            vendor: 'Test Vendor',
            contactName: 'John Doe',
            shelf: 'A-123',
            notes: 'Test notes',
            releasePattern: [1, 2, 3, 4, 5, 6, 7],
        });

        expect(mockMutateAsync).toHaveBeenCalledWith({
            data: {
                id: 123,
                vendor: 'Test Vendor',
                contactName: 'John Doe',
                shelf: 'A-123',
                notes: 'Test notes',
                contactInfos: undefined,
                releasePattern: [1, 2, 3, 4, 5, 6, 7],
            },
        });
    });

    it('trims whitespace from string fields', async () => {
        mockMutateAsync.mockResolvedValue({ id: 123 });

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({
            id: 123,
            vendor: '  Test Vendor  ',
            contactName: '  John Doe  ',
            shelf: '  A-123  ',
            notes: '  Test notes  ',
        });

        expect(mockMutateAsync).toHaveBeenCalledWith({
            data: {
                id: 123,
                vendor: 'Test Vendor',
                contactName: 'John Doe',
                shelf: 'A-123',
                notes: 'Test notes',
                contactInfos: undefined,
                releasePattern: undefined,
            },
        });
    });

    it('converts empty strings to undefined', async () => {
        mockMutateAsync.mockResolvedValue({ id: 456 });

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({ id: 456, vendor: '', contactName: '   ', shelf: '', notes: '' });

        expect(mockMutateAsync).toHaveBeenCalledWith({
            data: {
                id: 456,
                vendor: undefined,
                contactName: undefined,
                shelf: undefined,
                notes: undefined,
                contactInfos: undefined,
                releasePattern: undefined,
            },
        });
    });

    it('filters out empty contact infos', async () => {
        mockMutateAsync.mockResolvedValue({ id: 789 });

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({
            id: 789,
            contactInfos: [
                { contactType: 'PHONE' as any, contactValue: '12345678' },
                { contactType: null as any, contactValue: '98765432' },
                { contactType: 'EMAIL' as any, contactValue: '' },
                { contactType: 'EMAIL' as any, contactValue: '   ' },
            ],
        });

        const call = mockMutateAsync.mock.calls[0][0];
        expect(call.data.contactInfos).toEqual([{ contactType: 'PHONE', contactValue: '12345678' }]);
    });

    it('trims contact info values', async () => {
        mockMutateAsync.mockResolvedValue({ id: 111 });

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({
            id: 111,
            contactInfos: [{ contactType: 'PHONE' as any, contactValue: '  12345678  ' }],
        });

        const call = mockMutateAsync.mock.calls[0][0];
        expect(call.data.contactInfos).toEqual([{ contactType: 'PHONE', contactValue: '12345678' }]);
    });

    it('truncates release pattern numbers', async () => {
        mockMutateAsync.mockResolvedValue({ id: 222 });

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({ id: 222, releasePattern: [1.7, 2.3, 3.9, 4.1, 5.5, 6.6, 7.2] });

        const call = mockMutateAsync.mock.calls[0][0];
        expect(call.data.releasePattern).toEqual([1, 2, 3, 4, 5, 6, 7]);
    });

    it('updates query cache on success', async () => {
        const mockSavedTitle = { id: 333, vendor: 'Updated Vendor' };
        mockMutateAsync.mockResolvedValue(mockSavedTitle);

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({ id: 333, vendor: 'Updated Vendor' });

        const cachedData = queryClient.getQueryData(['huginTitle', 333]);
        expect(cachedData).toEqual(mockSavedTitle);
    });

    it('handles save errors', async () => {
        mockMutateAsync.mockRejectedValue(new Error('Save failed'));

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await expect(result.current.mutateAsync({ id: 444 })).rejects.toThrow('Save failed');
    });

    it('handles null values in payload', async () => {
        mockMutateAsync.mockResolvedValue({ id: 555 });

        const { result } = renderHook(() => useSaveHuginTitle(), { wrapper });

        await result.current.mutateAsync({ id: 555, vendor: null as any, contactName: null as any });

        expect(mockMutateAsync).toHaveBeenCalledWith({
            data: {
                id: 555,
                vendor: undefined,
                contactName: undefined,
                shelf: undefined,
                notes: undefined,
                contactInfos: undefined,
                releasePattern: undefined,
            },
        });
    });
});
