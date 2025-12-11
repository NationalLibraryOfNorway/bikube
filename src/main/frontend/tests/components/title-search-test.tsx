import 'vitest';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import TitleSearch from '@/components/title-search';

// Mock hooks
vi.mock('@/hooks/use-catalogue-title', () => ({
    useCatalogueTitles: () => ({
        catalogueTitlesList: [],
        isLoading: false
    })
}));

// Mock UI components
vi.mock('@/components/ui/command', () => ({
    Command: ({ children, className }: any) => <div className={className}>{children}</div>,
    CommandInput: ({ placeholder, value, onValueChange }: any) => (
        <input
            placeholder={placeholder}
            value={value}
            onChange={(e) => onValueChange?.(e.target.value)}
            data-testid="command-input"
        />
    ),
    CommandList: ({ children }: any) => <div>{children}</div>,
    CommandEmpty: ({ children }: any) => <div>{children}</div>,
    CommandItem: ({ children, onSelect }: any) => (
        <div onClick={onSelect}>{children}</div>
    )
}));

vi.mock('@/components/ui/popover', () => ({
    Popover: ({ children }: any) => <div>{children}</div>,
    PopoverTrigger: ({ children }: any) => <div>{children}</div>,
    PopoverContent: ({ children }: any) => <div>{children}</div>
}));

vi.mock('@/components/ui/badge', () => ({
    Badge: ({ children }: any) => <span>{children}</span>
}));

describe('TitleSearch', () => {
    const queryClient = new QueryClient();

    const renderComponent = (props = {}) => {
        return render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter>
                    <TitleSearch {...props} />
                </MemoryRouter>
            </QueryClientProvider>
        );
    };

    it('renders search input', () => {
        renderComponent();
        const input = screen.getByTestId('command-input');
        expect(input).toBeInTheDocument();
    });

    it('has correct placeholder text', () => {
        renderComponent();
        const input = screen.getByPlaceholderText('Søk etter avistittel…');
        expect(input).toBeInTheDocument();
    });

    it('applies custom className', () => {
        const { container } = renderComponent({ className: 'custom-class' });
        const searchWrapper = container.querySelector('.custom-class');
        expect(searchWrapper).toBeInTheDocument();
    });

    it('has default width styling', () => {
        const { container } = renderComponent();
        const wrapper = container.querySelector('.w-\\[25rem\\]');
        expect(wrapper).toBeInTheDocument();
    });

    it('has max-width constraint', () => {
        const { container } = renderComponent();
        const wrapper = container.querySelector('.max-w-\\[90vw\\]');
        expect(wrapper).toBeInTheDocument();
    });
});

