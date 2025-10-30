/// <reference types="vitest" />
import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import Logo from '@/components/logo';

describe('Logo', () => {
    it('renders SVG element', () => {
        const { container } = render(<Logo />);
        const svg = container.querySelector('svg');
        expect(svg).toBeInTheDocument();
    });

    it('applies custom className', () => {
        const { container } = render(<Logo className="custom-class" />);
        const svg = container.querySelector('svg');
        expect(svg).toHaveClass('custom-class');
    });

    it('has correct viewBox attribute', () => {
        const { container } = render(<Logo />);
        const svg = container.querySelector('svg');
        expect(svg).toHaveAttribute('viewBox', '-2062.85 987.74 249.8 292.65');
    });

    it('renders without className when not provided', () => {
        const { container } = render(<Logo />);
        const svg = container.querySelector('svg');
        expect(svg).not.toHaveClass('undefined');
    });
});

