/// <reference types="vitest" />
import { describe, it, expect, vi } from 'vitest';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import ContactForm from '@/components/contact-form';

vi.mock('@/hooks/use-save-hugin-title', () => ({
    useSaveHuginTitle: () => ({
        mutateAsync: vi.fn()
    })
}));

vi.mock('@/components/ui/button', () => ({
    Button: ({ children }: any) => <button>{children}</button>
}));

vi.mock('@/components/ui/input', () => ({
    Input: (props: any) => <input {...props} />
}));

vi.mock('@/components/ui/label', () => ({
    Label: ({ children }: any) => <label>{children}</label>
}));

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn()
    }
}));

describe('ContactForm', () => {
    const queryClient = new QueryClient();
    const mockTitle = {
        id: 123,
        vendor: 'Test Vendor',
        contactName: 'John Doe',
        phone: '12345678',
        email: 'test@example.com'
    };

    const renderComponent = (title: any = mockTitle, fields?: Array<'vendor' | 'contactName' | 'phone' | 'email' | 'shelf' | 'notes'>) => {
        return render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter>
                    <ContactForm title={title} fields={fields} />
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

    it('renders with custom fields', () => {
        const { container } = renderComponent(mockTitle, ['vendor', 'phone']);
        const form = container.querySelector('form');
        expect(form).toBeInTheDocument();
    });
});

