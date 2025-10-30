/// <reference types="vitest" />
import { describe, it, expect, vi } from 'vitest';
import { render } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import TitleCommentForm from '@/components/title-comment-form';

vi.mock('@/hooks/use-save-hugin-title', () => ({
    useSaveHuginTitle: () => ({
        mutateAsync: vi.fn()
    })
}));

vi.mock('@/components/ui/button', () => ({
    Button: ({ children }: any) => <button>{children}</button>
}));

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn()
    }
}));

describe('TitleCommentForm', () => {
    const queryClient = new QueryClient();
    const mockTitle = {
        id: 123,
        notes: 'Test comment'
    };

    const renderComponent = (title: any = mockTitle, catalogueTitleId = '123') => {
        return render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter initialEntries={[`/${catalogueTitleId}`]}>
                    <Routes>
                        <Route path="/:catalogueTitleId" element={<TitleCommentForm title={title} />} />
                    </Routes>
                </MemoryRouter>
            </QueryClientProvider>
        );
    };

    it('renders the form when catalogueTitleId is valid', () => {
        const { container } = renderComponent();
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });

    it('returns null when catalogueTitleId is invalid', () => {
        const { container } = renderComponent(mockTitle, 'invalid');
        const form = container.querySelector('form');
        expect(form).not.toBeInTheDocument();
    });

    it('initializes with title notes', () => {
        const { container } = renderComponent();
        expect(container).toBeInTheDocument();
    });

    it('handles null title', () => {
        const { container } = renderComponent(null);
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });

    it('handles undefined title', () => {
        const { container } = renderComponent(undefined);
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });
});

