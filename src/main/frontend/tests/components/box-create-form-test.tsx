/// <reference types="vitest" />
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import BoxCreateForm from '@/components/box-create-form';

// Mock services
vi.mock('@/generated/endpoints', () => ({
    HuginNewspaperService: {
        createBox: vi.fn()
    }
}));

// Mock UI components
vi.mock('@/components/ui/button', () => ({
    Button: ({ children, type }: any) => <button type={type}>{children}</button>
}));

vi.mock('@/components/ui/input', () => ({
    Input: (props: any) => <input {...props} />
}));

vi.mock('@/components/ui/label', () => ({
    Label: ({ children }: any) => <label>{children}</label>
}));

vi.mock('@/components/ui/calendar', () => ({
    Calendar: ({ selected, onSelect }: any) => (
        <div data-testid="calendar" onClick={() => onSelect?.(new Date())}>
            Calendar
        </div>
    )
}));

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn()
    }
}));

describe('BoxCreateForm', () => {
    const queryClient = new QueryClient();

    const renderComponent = (catalogueTitleId = '123') => {
        return render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter initialEntries={[`/${catalogueTitleId}`]}>
                    <Routes>
                        <Route path="/:catalogueTitleId" element={<BoxCreateForm />} />
                    </Routes>
                </MemoryRouter>
            </QueryClientProvider>
        );
    };

    it('renders the form', () => {
        const { container } = renderComponent();
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });

    it('returns null when catalogueTitleId is invalid', () => {
        const { container } = render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter initialEntries={['/invalid']}>
                    <Routes>
                        <Route path="/:catalogueTitleId" element={<BoxCreateForm />} />
                    </Routes>
                </MemoryRouter>
            </QueryClientProvider>
        );

        const form = container.querySelector('form');
        expect(form).not.toBeInTheDocument();
    });

    it('renders with valid catalogueTitleId', () => {
        const { container } = renderComponent('456');
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });
});

