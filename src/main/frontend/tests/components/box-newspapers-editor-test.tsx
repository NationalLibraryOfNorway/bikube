import 'vitest';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import BoxNewspapersEditor from '@/components/box-newspapers-editor';
import { server } from '../setup/server';

vi.mock('sonner', () => ({
    toast: { success: vi.fn(), error: vi.fn() },
}));

const makeWrapper = () => {
    const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } });
    return ({ children }: { children: React.ReactNode }) => (
        <QueryClientProvider client={queryClient}>
            <MemoryRouter>{children}</MemoryRouter>
        </QueryClientProvider>
    );
};

const makeTitle = (overrides: object = {}) => ({
    id: 123,
    releasePattern: [1, 1, 1, 1, 1, 0, 0],
    boxes: [
        {
            id: 'BOX-1',
            dateFrom: '2024-01-01',
            active: true,
            newspapers: [],
        },
    ],
    ...overrides,
});

const makeSavedTitle = () =>
    makeTitle({
        boxes: [
            {
                id: 'BOX-1',
                dateFrom: '2024-01-01',
                active: true,
                newspapers: [
                    { catalogId: 'CAT-001', edition: '1', date: '2024-01-01', received: false, notes: '', username: '' },
                    { catalogId: 'CAT-002', edition: '2', date: '2024-01-02', received: false, notes: '', username: '' },
                ],
            },
        ],
    });

describe('BoxNewspapersEditor — row management', () => {
    it('renders with no rows when box has no newspapers', () => {
        const { queryClient: _qc, wrapper } = { queryClient: new QueryClient(), wrapper: makeWrapper() };
        render(<BoxNewspapersEditor title={makeTitle() as any} />, { wrapper });
        expect(screen.getByText('Ingen utgaver lagt til.')).toBeInTheDocument();
    });

    it('renders existing saved newspapers as rows', () => {
        render(<BoxNewspapersEditor title={makeSavedTitle() as any} />, { wrapper: makeWrapper() });
        const inputs = screen.getAllByDisplayValue(/2024-01-0[12]/);
        expect(inputs).toHaveLength(2);
    });

    it('adds a new row when "Legg til ny utgave" is clicked', () => {
        render(<BoxNewspapersEditor title={makeTitle() as any} />, { wrapper: makeWrapper() });
        expect(screen.getByText('Ingen utgaver lagt til.')).toBeInTheDocument();

        fireEvent.click(screen.getByText(/Legg til ny utgave/));

        expect(screen.queryByText('Ingen utgaver lagt til.')).not.toBeInTheDocument();
        expect(screen.getAllByRole('row').length).toBeGreaterThan(1); // header + data row
    });

    it('removes an unsaved row locally without a network call', async () => {
        let deleteCalled = false;
        server.use(
            http.delete('*/api/hugin/newspapers/:id', () => {
                deleteCalled = true;
                return HttpResponse.json(null);
            })
        );

        render(<BoxNewspapersEditor title={makeTitle() as any} />, { wrapper: makeWrapper() });

        fireEvent.click(screen.getByText(/Legg til ny utgave/));
        expect(screen.queryByText('Ingen utgaver lagt til.')).not.toBeInTheDocument();

        const deleteBtn = screen.getByRole('button', { name: 'Slett utgave' });
        fireEvent.click(deleteBtn);

        await waitFor(() => {
            expect(screen.getByText('Ingen utgaver lagt til.')).toBeInTheDocument();
        });
        expect(deleteCalled).toBe(false);
    });

    it('calls DELETE endpoint when removing a saved newspaper row', async () => {
        server.use(
            http.delete('*/api/hugin/newspapers/:id', () => HttpResponse.json(null))
        );

        render(<BoxNewspapersEditor title={makeSavedTitle() as any} />, { wrapper: makeWrapper() });

        const deleteButtons = screen.getAllByRole('button', { name: 'Slett utgave' });
        expect(deleteButtons).toHaveLength(2);

        fireEvent.click(deleteButtons[0]);

        await waitFor(() => {
            expect(screen.getAllByRole('button', { name: 'Slett utgave' })).toHaveLength(1);
        });
    });

    it('suggests the next date when adding multiple rows', () => {
        render(<BoxNewspapersEditor title={makeTitle() as any} />, { wrapper: makeWrapper() });

        fireEvent.click(screen.getByText(/Legg til ny utgave/));
        fireEvent.click(screen.getByText(/Legg til ny utgave/));

        const dateInputs = screen.getAllByDisplayValue(/^\d{4}-\d{2}-\d{2}$/);
        expect(dateInputs).toHaveLength(2);
        expect(dateInputs[0].getAttribute('value')).not.toBe(dateInputs[1].getAttribute('value'));
    });

    it('suggests the next edition number when adding a row after existing ones', () => {
        render(<BoxNewspapersEditor title={makeSavedTitle() as any} />, { wrapper: makeWrapper() });

        fireEvent.click(screen.getByText(/Legg til ny utgave/));

        // Edition inputs use inputMode="numeric", not type="number", so role is textbox
        const editionInputs = document.querySelectorAll('input[inputmode="numeric"]');
        // The new row should have edition "3" (max of 1,2 + 1)
        const values = Array.from(editionInputs).map(i => (i as HTMLInputElement).value);
        expect(values).toContain('3');
    });
});

describe('BoxNewspapersEditor — save', () => {
    it('sends all rows to the batch endpoint on save', async () => {
        let capturedBody: unknown;
        server.use(
            http.post('*/api/hugin/newspapers/batch', async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json([]);
            })
        );

        render(<BoxNewspapersEditor title={makeTitle() as any} />, { wrapper: makeWrapper() });

        fireEvent.click(screen.getByText(/Legg til ny utgave/));

        const saveButton = screen.getByText(/Lagre/);
        fireEvent.click(saveButton);

        await waitFor(() => expect(capturedBody).toBeTruthy());
        expect(Array.isArray(capturedBody)).toBe(true);
        expect((capturedBody as any[]).length).toBe(1);
        expect((capturedBody as any[])[0].titleId).toBe(123);
    });

    it('does not send a request when there are no rows', async () => {
        let batchCalled = false;
        server.use(
            http.post('*/api/hugin/newspapers/batch', () => {
                batchCalled = true;
                return HttpResponse.json([]);
            })
        );

        render(<BoxNewspapersEditor title={makeTitle() as any} />, { wrapper: makeWrapper() });

        fireEvent.click(screen.getByText(/Lagre/));

        await new Promise(r => setTimeout(r, 50));
        expect(batchCalled).toBe(false);
    });
});

describe('BoxNewspapersEditor — edge cases', () => {
    it('returns null when title has no active box', () => {
        const title = makeTitle({ boxes: [{ id: 'BOX-1', dateFrom: '2024-01-01', active: false, newspapers: [] }] });
        const { container } = render(<BoxNewspapersEditor title={title as any} />, { wrapper: makeWrapper() });
        expect(container.firstChild).toBeNull();
    });

    it('throws when title is null', () => {
        const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
        expect(() => render(<BoxNewspapersEditor title={null as any} />, { wrapper: makeWrapper() })).toThrow();
        spy.mockRestore();
    });
});
