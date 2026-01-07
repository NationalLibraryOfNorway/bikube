import 'vitest';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import ReleasePattern from '@/components/release-pattern';

describe('ReleasePattern', () => {
    const daysOfWeek = ['Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag', 'Søndag'];

    it('renders the title', () => {
        render(<ReleasePattern releasePattern={[1, 2, 3, 4, 5, 6, 7]} />);
        expect(screen.getByText('Utgivelsesmønster:')).toBeInTheDocument();
    });

    it('renders all days of the week', () => {
        render(<ReleasePattern releasePattern={[1, 2, 3, 4, 5, 6, 7]} />);

        daysOfWeek.forEach(day => {
            expect(screen.getByText(`${day}:`)).toBeInTheDocument();
        });
    });

    it('renders the correct pattern values for each day', () => {
        const pattern = [1, 2, 0, 3, 0, 1, 0];
        render(<ReleasePattern releasePattern={pattern} />);

        pattern.forEach(value => {
            expect(screen.getAllByText(value.toString()).length).toBeGreaterThan(0);
        });
    });

    it('renders pattern with all zeros', () => {
        const pattern = [0, 0, 0, 0, 0, 0, 0];
        render(<ReleasePattern releasePattern={pattern} />);

        const zeros = screen.getAllByText('0');
        expect(zeros.length).toBe(7);
    });

    it('renders pattern with all same values', () => {
        const pattern = [3, 3, 3, 3, 3, 3, 3];
        render(<ReleasePattern releasePattern={pattern} />);

        const threes = screen.getAllByText('3');
        expect(threes.length).toBe(7);
    });

    it('renders table with correct structure', () => {
        const { container } = render(<ReleasePattern releasePattern={[1, 2, 3, 4, 5, 6, 7]} />);

        const table = container.querySelector('table');
        expect(table).toBeInTheDocument();

        const tbody = container.querySelector('tbody');
        expect(tbody).toBeInTheDocument();

        const rows = container.querySelectorAll('tr');
        expect(rows.length).toBe(7);
    });

    it('renders Monday with correct pattern value', () => {
        render(<ReleasePattern releasePattern={[5, 0, 0, 0, 0, 0, 0]} />);

        const mondayRow = screen.getByText('Mandag:').closest('tr');
        expect(mondayRow).toHaveTextContent('5');
    });

    it('renders Sunday with correct pattern value', () => {
        render(<ReleasePattern releasePattern={[0, 0, 0, 0, 0, 0, 9]} />);

        const sundayRow = screen.getByText('Søndag:').closest('tr');
        expect(sundayRow).toHaveTextContent('9');
    });

    it('handles empty pattern gracefully', () => {
        render(<ReleasePattern releasePattern={[]} />);

        daysOfWeek.forEach(day => {
            expect(screen.getByText(`${day}:`)).toBeInTheDocument();
        });
    });
});

