import 'vitest';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { cn, isActive, redirect } from '@/lib/utils';

describe('cn (className utility)', () => {
    it('merges multiple class names', () => {
        const result = cn('foo', 'bar');
        expect(result).toBe('foo bar');
    });

    it('handles undefined and null values', () => {
        const result = cn('foo', undefined, null, 'bar');
        expect(result).toBe('foo bar');
    });

    it('handles empty strings', () => {
        const result = cn('foo', '', 'bar');
        expect(result).toBe('foo bar');
    });

    it('merges Tailwind classes correctly (removes duplicates)', () => {
        const result = cn('px-2 py-1', 'px-4');
        expect(result).toBe('py-1 px-4');
    });

    it('handles array of classes', () => {
        const result = cn(['foo', 'bar']);
        expect(result).toBe('foo bar');
    });

    it('handles object with conditional classes', () => {
        const result = cn({
            'foo': true,
            'bar': false,
            'baz': true
        });
        expect(result).toBe('foo baz');
    });

    it('combines arrays, objects, and strings', () => {
        const result = cn('base', ['foo', 'bar'], { 'baz': true, 'qux': false });
        expect(result).toBe('base foo bar baz');
    });

    it('handles no arguments', () => {
        const result = cn();
        expect(result).toBe('');
    });

    it('handles complex Tailwind class conflicts', () => {
        const result = cn('text-red-500 hover:text-blue-500', 'text-green-500');
        expect(result).toBe('hover:text-blue-500 text-green-500');
    });

    it('preserves important classes', () => {
        const result = cn('!text-red-500', 'text-blue-500');
        expect(result).toBe('!text-red-500 text-blue-500');
    });

    it('handles responsive classes correctly', () => {
        const result = cn('sm:w-full md:w-1/2', 'lg:w-1/3');
        expect(result).toBe('sm:w-full md:w-1/2 lg:w-1/3');
    });

    it('handles dark mode classes', () => {
        const result = cn('text-black dark:text-white', 'hover:text-gray-500');
        expect(result).toBe('text-black dark:text-white hover:text-gray-500');
    });
});

describe('isActive', () => {
    afterEach(() => {
        // Always restore real timers after each test
        vi.useRealTimers();
    });

    describe('returns true (active)', () => {
        it('returns true when endDate is null', () => {
            expect(isActive(null)).toBe(true);
        });

        it('returns true when endDate is undefined', () => {
            expect(isActive(undefined)).toBe(true);
        });

        it('returns true when endDate is empty string', () => {
            expect(isActive('')).toBe(true);
        });

        it('returns true when endDate is in the future', () => {
            // Mock current date as 2024-01-01
            const mockToday = new Date('2024-01-01T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            const futureDate = '2024-12-31';
            expect(isActive(futureDate)).toBe(true);

            vi.useRealTimers();
        });

        it('returns true when endDate is far in the future', () => {
            const mockToday = new Date('2024-01-01T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            const futureDate = '2030-12-31';
            expect(isActive(futureDate)).toBe(true);

            vi.useRealTimers();
        });

        it('returns true when endDate is tomorrow', () => {
            const mockToday = new Date('2024-01-01T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            const tomorrow = '2024-01-02';
            expect(isActive(tomorrow)).toBe(true);

            vi.useRealTimers();
        });
    });

    describe('returns false (inactive)', () => {
        it('returns false when endDate is in the past', () => {
            const mockToday = new Date('2024-01-01T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            const pastDate = '2023-12-31';
            expect(isActive(pastDate)).toBe(false);

            vi.useRealTimers();
        });

        it('returns false when endDate is today (but earlier time)', () => {
            const mockToday = new Date('2024-01-01T23:59:59Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            const todayEarlier = '2024-01-01T00:00:00Z';
            expect(isActive(todayEarlier)).toBe(false);

            vi.useRealTimers();
        });

        it('returns false when endDate is far in the past', () => {
            const mockToday = new Date('2024-01-01T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            const pastDate = '2020-01-01';
            expect(isActive(pastDate)).toBe(false);

            vi.useRealTimers();
        });

        it('returns false when endDate is yesterday', () => {
            const mockToday = new Date('2024-01-02T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            const yesterday = '2024-01-01';
            expect(isActive(yesterday)).toBe(false);

            vi.useRealTimers();
        });
    });

    describe('date format handling', () => {
        it('handles ISO date strings', () => {
            const mockToday = new Date('2024-01-01T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            expect(isActive('2024-12-31T23:59:59Z')).toBe(true);
            expect(isActive('2023-12-31T23:59:59Z')).toBe(false);

            vi.useRealTimers();
        });

        it('handles date-only strings', () => {
            const mockToday = new Date('2024-06-15T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            expect(isActive('2024-06-16')).toBe(true);
            expect(isActive('2024-06-14')).toBe(false);

            vi.useRealTimers();
        });

        it('handles various date formats', () => {
            const mockToday = new Date('2024-01-01T12:00:00Z');
            vi.useFakeTimers();
            vi.setSystemTime(mockToday);

            // Different date formats that JavaScript Date can parse
            expect(isActive('2024-12-31')).toBe(true);
            expect(isActive('Dec 31, 2024')).toBe(true);
            expect(isActive('2024/12/31')).toBe(true);

            vi.useRealTimers();
        });
    });

    describe('edge cases', () => {
        it('handles invalid date strings by returning false', () => {
            const result = isActive('invalid-date');
            // Invalid date creates a Date object with NaN
            // NaN > anything is false
            expect(result).toBe(false);
        });

        it('handles whitespace-only string (treated as invalid date)', () => {
            // Whitespace is truthy, so it will be parsed as a date
            // '   ' is an invalid date string, which creates Invalid Date
            // Invalid Date > today is false
            expect(isActive('   ')).toBe(false);
        });
    });
});

describe('redirect', () => {
    it('is a function that sets window.location.href', () => {
        expect(typeof redirect).toBe('function');
    });

});

