/// <reference types="vitest" />
import { describe, it, expect } from 'vitest';
import { validateBetweenZeroAndFive, checkDuplicateEditions } from '@/utils/validation-utils';

describe('validateBetweenZeroAndFive', () => {
    describe('valid values', () => {
        it('returns undefined for 0', () => {
            expect(validateBetweenZeroAndFive(0)).toBeUndefined();
        });

        it('returns undefined for 1', () => {
            expect(validateBetweenZeroAndFive(1)).toBeUndefined();
        });

        it('returns undefined for 2', () => {
            expect(validateBetweenZeroAndFive(2)).toBeUndefined();
        });

        it('returns undefined for 3', () => {
            expect(validateBetweenZeroAndFive(3)).toBeUndefined();
        });

        it('returns undefined for 4', () => {
            expect(validateBetweenZeroAndFive(4)).toBeUndefined();
        });

        it('returns undefined for 5', () => {
            expect(validateBetweenZeroAndFive(5)).toBeUndefined();
        });

        it('returns undefined for decimal within range (2.5)', () => {
            expect(validateBetweenZeroAndFive(2.5)).toBeUndefined();
        });

        it('returns undefined for decimal at lower bound (0.1)', () => {
            expect(validateBetweenZeroAndFive(0.1)).toBeUndefined();
        });

        it('returns undefined for decimal at upper bound (4.9)', () => {
            expect(validateBetweenZeroAndFive(4.9)).toBeUndefined();
        });
    });

    describe('invalid values - negative', () => {
        it('returns error for -1', () => {
            expect(validateBetweenZeroAndFive(-1)).toBe('Tallet kan ikke være negativt');
        });

        it('returns error for -0.1', () => {
            expect(validateBetweenZeroAndFive(-0.1)).toBe('Tallet kan ikke være negativt');
        });

        it('returns error for -100', () => {
            expect(validateBetweenZeroAndFive(-100)).toBe('Tallet kan ikke være negativt');
        });

        it('returns error for negative infinity', () => {
            expect(validateBetweenZeroAndFive(-Infinity)).toBe('Tallet kan ikke være negativt');
        });
    });

    describe('invalid values - too large', () => {
        it('returns error for 6', () => {
            expect(validateBetweenZeroAndFive(6)).toBe('Tallet kan ikke være større enn 5');
        });

        it('returns error for 5.1', () => {
            expect(validateBetweenZeroAndFive(5.1)).toBe('Tallet kan ikke være større enn 5');
        });

        it('returns error for 10', () => {
            expect(validateBetweenZeroAndFive(10)).toBe('Tallet kan ikke være større enn 5');
        });

        it('returns error for 100', () => {
            expect(validateBetweenZeroAndFive(100)).toBe('Tallet kan ikke være større enn 5');
        });

        it('returns error for positive infinity', () => {
            expect(validateBetweenZeroAndFive(Infinity)).toBe('Tallet kan ikke være større enn 5');
        });
    });

    describe('edge cases', () => {
        it('handles NaN (returns undefined as NaN fails both conditions)', () => {
            // NaN < 0 is false, NaN > 5 is false
            expect(validateBetweenZeroAndFive(NaN)).toBeUndefined();
        });
    });
});

describe('checkDuplicateEditions', () => {
    describe('no duplicates', () => {
        it('returns empty string for empty array', () => {
            expect(checkDuplicateEditions([])).toBe('');
        });

        it('returns empty string for single edition', () => {
            expect(checkDuplicateEditions(['1'])).toBe('');
        });

        it('returns empty string for unique editions', () => {
            expect(checkDuplicateEditions(['1', '2', '3', '4', '5'])).toBe('');
        });

        it('returns empty string for unique editions with gaps', () => {
            expect(checkDuplicateEditions(['1', '5', '10'])).toBe('');
        });

        it('returns empty string when array contains empty strings', () => {
            expect(checkDuplicateEditions(['1', '', '2', ''])).toBe('');
        });

        it('returns empty string when array contains only empty strings', () => {
            expect(checkDuplicateEditions(['', '', ''])).toBe('');
        });

        it('returns empty string for editions with different formats', () => {
            expect(checkDuplicateEditions(['1', '01', '001'])).toBe('');
        });
    });

    describe('with duplicates', () => {
        it('returns error for duplicate edition at start', () => {
            expect(checkDuplicateEditions(['1', '1', '2', '3'])).toBe('Det fins duplikate utgavenummer');
        });

        it('returns error for duplicate edition at end', () => {
            expect(checkDuplicateEditions(['1', '2', '3', '3'])).toBe('Det fins duplikate utgavenummer');
        });

        it('returns error for duplicate edition in middle', () => {
            expect(checkDuplicateEditions(['1', '2', '2', '3'])).toBe('Det fins duplikate utgavenummer');
        });

        it('returns error for duplicate edition far apart', () => {
            expect(checkDuplicateEditions(['1', '2', '3', '4', '5', '1'])).toBe('Det fins duplikate utgavenummer');
        });

        it('returns error for all same editions', () => {
            expect(checkDuplicateEditions(['1', '1', '1', '1'])).toBe('Det fins duplikate utgavenummer');
        });

        it('returns error for two pairs of duplicates', () => {
            expect(checkDuplicateEditions(['1', '1', '2', '2'])).toBe('Det fins duplikate utgavenummer');
        });

        it('returns error for triple duplicate', () => {
            expect(checkDuplicateEditions(['1', '2', '1', '3', '1'])).toBe('Det fins duplikate utgavenummer');
        });
    });

    describe('edge cases with whitespace and falsy values', () => {
        it('ignores empty strings when checking duplicates', () => {
            expect(checkDuplicateEditions(['', '1', '', '2'])).toBe('');
        });

        it('returns empty string for array with null-like values', () => {
            // Since the filter checks !!v, null/undefined would be filtered out
            expect(checkDuplicateEditions(['1', null as any, '2', null as any])).toBe('');
        });

        it('detects duplicates when non-empty values are present', () => {
            expect(checkDuplicateEditions(['', '1', '1', ''])).toBe('Det fins duplikate utgavenummer');
        });
    });

    describe('string editions', () => {
        it('detects duplicate string editions', () => {
            expect(checkDuplicateEditions(['A', 'B', 'A'])).toBe('Det fins duplikate utgavenummer');
        });

        it('treats different strings as unique', () => {
            expect(checkDuplicateEditions(['A', 'a', 'B', 'b'])).toBe('');
        });

        it('detects duplicate with special characters', () => {
            expect(checkDuplicateEditions(['1-A', '2-B', '1-A'])).toBe('Det fins duplikate utgavenummer');
        });

        it('handles whitespace in editions', () => {
            expect(checkDuplicateEditions(['1 ', '1 ', '2'])).toBe('Det fins duplikate utgavenummer');
        });

        it('treats editions with different whitespace as different', () => {
            expect(checkDuplicateEditions(['1', '1 ', ' 1'])).toBe('');
        });
    });

    describe('performance with large arrays', () => {
        it('handles large array without duplicates efficiently', () => {
            const editions = Array.from({ length: 100 }, (_, i) => String(i));
            expect(checkDuplicateEditions(editions)).toBe('');
        });

        it('detects duplicate early in large array', () => {
            const editions = Array.from({ length: 100 }, (_, i) => String(i));
            editions.push('0'); // Add duplicate at the end
            expect(checkDuplicateEditions(editions)).toBe('Det fins duplikate utgavenummer');
        });
    });
});

