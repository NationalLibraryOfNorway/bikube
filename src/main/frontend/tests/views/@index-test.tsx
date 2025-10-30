/// <reference types="vitest" />
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import MainView from '@/views/@index';

// Mock the components to avoid complex dependencies
vi.mock('@/components/logo', () => ({
    default: ({ className }: { className?: string }) => <div data-testid="logo" className={className}>Logo</div>
}));

vi.mock('@/components/title-search', () => ({
    default: ({ className }: { className?: string }) => <div data-testid="title-search" className={className}>Title Search</div>
}));

describe('MainView (@index)', () => {
    it('renders the logo component', () => {
        render(<MainView />);
        const logo = screen.getByTestId('logo');
        expect(logo).toBeInTheDocument();
        expect(logo).toHaveClass('w-[150px]', 'mb-5');
    });

    it('renders the title search component', () => {
        render(<MainView />);
        const titleSearch = screen.getByTestId('title-search');
        expect(titleSearch).toBeInTheDocument();
        expect(titleSearch).toHaveClass('p-3');
    });

    it('has the correct layout structure', () => {
        const { container } = render(<MainView />);
        const wrapper = container.firstChild as HTMLElement;
        expect(wrapper).toHaveClass('flex', 'flex-col', 'items-center', 'mt-25');
    });
});
