/// <reference types="vitest" />
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import Header from '@/components/header';
import userEvent from '@testing-library/user-event';

// Mock child components
vi.mock('@/components/logo', () => ({
    default: ({ className }: { className?: string }) => (
        <div data-testid="logo" className={className}>Logo</div>
    )
}));

vi.mock('@/components/title-search', () => ({
    default: ({ className }: { className?: string }) => (
        <div data-testid="title-search" className={className}>Title Search</div>
    )
}));

vi.mock('@/components/ui/avatar', () => ({
    Avatar: ({ children }: { children: React.ReactNode }) => <div data-testid="avatar">{children}</div>,
    AvatarFallback: ({ children, className }: { children: React.ReactNode; className?: string }) => (
        <div data-testid="avatar-fallback" className={className}>{children}</div>
    )
}));

vi.mock('@/components/ui/button', () => ({
    Button: ({ children, onClick, variant }: { children: React.ReactNode; onClick: () => void; variant?: string }) => (
        <button data-testid="logout-button" onClick={onClick} data-variant={variant}>{children}</button>
    )
}));

vi.mock('@vaadin/react-components', () => ({
    HorizontalLayout: ({ children, className, theme }: any) => (
        <div className={className} data-theme={theme}>{children}</div>
    )
}));

describe('Header', () => {
    const defaultProps = {
        onToggleTheme: vi.fn(),
        onLogout: vi.fn(),
        user: {
            initials: 'JD',
            fullName: 'John Doe'
        }
    };

    const renderWithRouter = (props = defaultProps, initialPath = '/') => {
        return render(
            <MemoryRouter initialEntries={[initialPath]}>
                <Header {...props} />
            </MemoryRouter>
        );
    };

    it('renders the logo', () => {
        renderWithRouter();
        expect(screen.getByTestId('logo')).toBeInTheDocument();
    });

    it('renders the Hugin title', () => {
        renderWithRouter();
        expect(screen.getByText('Hugin')).toBeInTheDocument();
    });

    it('renders user initials in avatar', () => {
        renderWithRouter();
        const avatarFallback = screen.getByTestId('avatar-fallback');
        expect(avatarFallback).toHaveTextContent('JD');
    });

    it('renders user full name', () => {
        renderWithRouter();
        expect(screen.getByText('John Doe')).toBeInTheDocument();
    });

    it('renders logout button', () => {
        renderWithRouter();
        expect(screen.getByTestId('logout-button')).toBeInTheDocument();
    });

    it('calls onLogout when logout button is clicked', async () => {
        const onLogout = vi.fn();
        renderWithRouter({ ...defaultProps, onLogout });
        const user = userEvent.setup();

        const logoutButton = screen.getByTestId('logout-button');
        await user.click(logoutButton);

        expect(onLogout).toHaveBeenCalledTimes(1);
    });

    it('does not show TitleSearch on home page', () => {
        renderWithRouter(defaultProps, '/');
        expect(screen.queryByTestId('title-search')).not.toBeInTheDocument();
    });

    it('shows TitleSearch on other pages', () => {
        renderWithRouter(defaultProps, '/some-other-page');
        expect(screen.getByTestId('title-search')).toBeInTheDocument();
    });

    it('renders without user data', () => {
        const propsWithoutUser: typeof defaultProps = {
            onToggleTheme: defaultProps.onToggleTheme,
            onLogout: defaultProps.onLogout,
            user: {initials: '', fullName: '' }
        };
        renderWithRouter(propsWithoutUser as any);

        const avatarFallback = screen.getByTestId('avatar-fallback');
        expect(avatarFallback).toBeInTheDocument();
    });

    it('logo links to home page', () => {
        renderWithRouter();
        const logoLink = screen.getByRole('link');
        expect(logoLink).toHaveAttribute('href', '/');
    });

    it('applies correct className to logo', () => {
        renderWithRouter();
        const logo = screen.getByTestId('logo');
        expect(logo).toHaveClass('w-[45px]');
    });
});

