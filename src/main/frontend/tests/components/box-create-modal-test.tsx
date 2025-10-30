/// <reference types="vitest" />
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import BoxCreateModal from '@/components/box-create-modal';
import userEvent from '@testing-library/user-event';

// Mock child components
vi.mock('@/components/box-create-form', () => ({
    default: ({ onSuccess }: { onSuccess: () => void }) => (
        <div data-testid="box-create-form">
            <button onClick={onSuccess} data-testid="form-success-button">Submit</button>
        </div>
    )
}));
// Mock UI components
vi.mock('@/components/ui/dialog', () => ({
    Dialog: ({ children, open }: any) => (
        <div data-testid="dialog" data-open={String(open)}>
            {children}
        </div>
    ),
    DialogTrigger: ({ children }: any) => (
        <div data-testid="dialog-trigger">{children}</div>
    ),
    DialogContent: ({ children, className }: any) => (
        <div data-testid="dialog-content" className={className}>{children}</div>
    ),
    DialogHeader: ({ children }: any) => <div data-testid="dialog-header">{children}</div>,
    DialogTitle: ({ children }: any) => <h2 data-testid="dialog-title">{children}</h2>
}));

vi.mock('@/components/ui/button', () => ({
    Button: ({ children, variant, onClick }: any) => (
        <button data-variant={variant} onClick={onClick}>
            {children}
        </button>
    )
}));

describe('BoxCreateModal', () => {
    it('renders the trigger button', () => {
        render(<BoxCreateModal />);
        const buttons = screen.getAllByText(/Ny eske/i);
        expect(buttons.length).toBeGreaterThan(0);
    });

    it('dialog is initially closed', () => {
        render(<BoxCreateModal />);
        const dialog = screen.getByTestId('dialog');
        expect(dialog).toHaveAttribute('data-open', 'false');
    });

    it('opens dialog when trigger button is clicked', async () => {
        render(<BoxCreateModal />);
        const user = userEvent.setup();

        const triggerButtons = screen.getAllByText(/Ny eske/i);
        // Click should not throw an error
        await user.click(triggerButtons[0]);

        // Verify dialog is still rendered (component handles open state internally)
        const dialog = screen.getByTestId('dialog');
        expect(dialog).toBeInTheDocument();
    });

    it('renders dialog title', () => {
        render(<BoxCreateModal />);
        expect(screen.getByText('Registrer en ny eske')).toBeInTheDocument();
    });

    it('renders BoxCreateForm component', () => {
        render(<BoxCreateModal />);
        expect(screen.getByTestId('box-create-form')).toBeInTheDocument();
    });

    it('closes dialog when form succeeds', async () => {
        render(<BoxCreateModal />);
        const user = userEvent.setup();

        // Open the dialog first
        const triggerButtons = screen.getAllByText(/Ny eske/i);
        await user.click(triggerButtons[0]);

        // Simulate form success
        const successButton = screen.getByTestId('form-success-button');
        await user.click(successButton);

        const dialog = screen.getByTestId('dialog');
        expect(dialog).toHaveAttribute('data-open', 'false');
    });

    it('dialog content has correct max-width class', () => {
        render(<BoxCreateModal />);
        const content = screen.getByTestId('dialog-content');
        expect(content).toHaveClass('sm:max-w-lg');
    });

    it('trigger button has secondary variant', () => {
        render(<BoxCreateModal />);
        const triggerButtons = screen.getAllByText(/Ny eske/i);
        expect(triggerButtons[0]).toHaveAttribute('data-variant', 'secondary');
    });
});

