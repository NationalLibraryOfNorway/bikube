import 'vitest';
import { describe, it, expect, vi } from 'vitest';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import ReleasePatternForm from '@/components/release-pattern-form';

vi.mock('@/hooks/use-save-hugin-title', () => ({
    useSaveHuginTitle: () => ({
        mutateAsync: vi.fn()
    })
}));

vi.mock('@/components/number-input', () => ({
    default: ({ value, onChange }: any) => (
        <input type="number" value={value} onChange={(e) => onChange(Number(e.target.value))} />
    )
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

describe('ReleasePatternForm', () => {
    const queryClient = new QueryClient();
    const mockTitle = {
        id: 123,
        releasePattern: [1, 2, 0, 3, 0, 1, 0]
    };

    const renderComponent = (title: any = mockTitle) => {
        return render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter>
                    <ReleasePatternForm title={title} />
                </MemoryRouter>
            </QueryClientProvider>
        );
    };

    it('renders the form', () => {
        const { container } = renderComponent();
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });

    it('renders with null title', () => {
        const { container } = renderComponent(null);
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });

    it('renders with undefined title', () => {
        const { container } = renderComponent(undefined);
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });

    it('renders with title without release pattern', () => {
        const titleWithoutPattern = { id: 123 };
        const { container } = renderComponent(titleWithoutPattern);
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });
});

