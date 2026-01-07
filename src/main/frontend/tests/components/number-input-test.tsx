import 'vitest';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import NumberInputWithButtons from '@/components/number-input';
import userEvent from '@testing-library/user-event';

vi.mock('@/components/ui/button', () => ({
    Button: ({ children, onClick, size, type, className }: any) => (
        <button onClick={onClick} data-size={size} type={type} className={className}>
            {children}
        </button>
    )
}));

describe('NumberInputWithButtons', () => {
    it('renders the current value', () => {
        const onChange = vi.fn();
        render(<NumberInputWithButtons value={5} onChange={onChange} />);
        
        expect(screen.getByText('5')).toBeInTheDocument();
    });

    it('increments value when plus button is clicked', async () => {
        const onChange = vi.fn();
        const user = userEvent.setup();
        const { container } = render(<NumberInputWithButtons value={5} onChange={onChange} />);
        
        const buttons = container.querySelectorAll('button');
        const plusButton = buttons[1]; // Second button is plus
        await user.click(plusButton);
        
        expect(onChange).toHaveBeenCalledWith(6);
    });

    it('decrements value when minus button is clicked', async () => {
        const onChange = vi.fn();
        const user = userEvent.setup();
        const { container } = render(<NumberInputWithButtons value={5} onChange={onChange} />);
        
        const buttons = container.querySelectorAll('button');
        const minusButton = buttons[0]; // First button is minus
        await user.click(minusButton);
        
        expect(onChange).toHaveBeenCalledWith(4);
    });

    it('respects minimum value', async () => {
        const onChange = vi.fn();
        const user = userEvent.setup();
        const { container } = render(<NumberInputWithButtons value={1} min={1} onChange={onChange} />);
        
        const buttons = container.querySelectorAll('button');
        const minusButton = buttons[0];
        await user.click(minusButton);
        
        expect(onChange).toHaveBeenCalledWith(1);
    });

    it('respects maximum value', async () => {
        const onChange = vi.fn();
        const user = userEvent.setup();
        const { container } = render(<NumberInputWithButtons value={10} max={10} onChange={onChange} />);
        
        const buttons = container.querySelectorAll('button');
        const plusButton = buttons[1];
        await user.click(plusButton);
        
        expect(onChange).toHaveBeenCalledWith(10);
    });

    it('uses custom step value', async () => {
        const onChange = vi.fn();
        const user = userEvent.setup();
        const { container } = render(<NumberInputWithButtons value={5} step={5} onChange={onChange} />);
        
        const buttons = container.querySelectorAll('button');
        const plusButton = buttons[1];
        await user.click(plusButton);
        
        expect(onChange).toHaveBeenCalledWith(10);
    });

    it('displays 0 when value is not finite', () => {
        const onChange = vi.fn();
        render(<NumberInputWithButtons value={NaN} onChange={onChange} />);
        
        expect(screen.getByText('0')).toBeInTheDocument();
    });

    it('handles undefined value as 0', async () => {
        const onChange = vi.fn();
        const user = userEvent.setup();
        const { container } = render(<NumberInputWithButtons value={undefined as any} onChange={onChange} />);
        
        const buttons = container.querySelectorAll('button');
        const plusButton = buttons[1];
        await user.click(plusButton);
        
        expect(onChange).toHaveBeenCalledWith(1);
    });

    it('renders with aria-live attribute for accessibility', () => {
        const onChange = vi.fn();
        const { container } = render(<NumberInputWithButtons value={5} onChange={onChange} />);
        
        const valueDisplay = container.querySelector('[aria-live="polite"]');
        expect(valueDisplay).toBeInTheDocument();
        expect(valueDisplay).toHaveAttribute('aria-atomic', 'true');
    });

    it('buttons have correct types', () => {
        const onChange = vi.fn();
        const { container } = render(<NumberInputWithButtons value={5} onChange={onChange} />);
        
        const buttons = container.querySelectorAll('button');
        buttons.forEach(button => {
            expect(button).toHaveAttribute('type', 'button');
        });
    });
});

