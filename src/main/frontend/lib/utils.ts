import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}

function levenshtein(a: string, b: string): number {
    /**
     * Uses the Wagner-Fischer algorithm to return the levenshtein distance between two strings
    **/
    const tmp: number[][] = [];
    let i = a.length;
    let j = b.length;

    if (i === 0) return j;
    if (j === 0) return i;

    for (let i = 0; i <= a.length; i++) tmp[i] = [i];
    for (let j = 0; j <= b.length; j++) tmp[0][j] = j;

    for (let i = 1; i <= a.length; i++) {
        for (let j = 1; j <= b.length; j++) {
            tmp[i][j] = Math.min(
                tmp[i - 1][j] + 1,  // Deletion
                tmp[i][j - 1] + 1,  // Insertion
                tmp[i - 1][j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1) // Substitution
            );
        }
    }
    return tmp[i][j];
}

function similarityScore(a: string, b: string): number {
    const distance = levenshtein(a, b);
    const maxLength = Math.max(a.length, b.length);
    return (maxLength - distance) / maxLength;  // Normalized similarity score (0 to 1)
}

export function sortListBySimilarityScore(listToBeSorted: any[], searchTerm: string, fieldToSortBy: string) {
    return listToBeSorted.sort((a, b) => {
        const valueA = (a[fieldToSortBy] ?? a).toLowerCase();
        const valueB = (b[fieldToSortBy] ?? b).toLowerCase();
        const scoreA = similarityScore(searchTerm.toLowerCase(), valueA);
        const scoreB = similarityScore(searchTerm.toLowerCase(), valueB);
        return scoreB - scoreA;
    });
}

/**
 * Interceptable redirect function that allows for testing
 */
export const redirect = (to: string) => {
    window.location.href = to;
}

export function getPagesButtonsList(currentPage: number, totalPages: number): (number | 'ellipsis')[] {
    currentPage = currentPage + 1 // Page is zero indexed
    let pageArr: (number | 'ellipsis')[] = []
    if (totalPages <= 7) pageArr = Array.from({ length: totalPages }, (_, i) => i + 1);
    else if (currentPage <= 4) pageArr = [1, 2, 3, 4, 5, 'ellipsis', totalPages]
    else if (currentPage >= (totalPages - 3)) pageArr = [1, 'ellipsis', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]
    else pageArr = [1, 'ellipsis', currentPage - 1, currentPage, currentPage + 1, 'ellipsis', totalPages]
    return pageArr
}
