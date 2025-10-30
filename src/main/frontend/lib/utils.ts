import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}

export const isActive = (endDate?: string | null) => {
    if (!endDate) return true;
    const end = new Date(endDate);
    const today = new Date();
    return end > today;
};

/**
 * Interceptable redirect function that allows for testing
 */
export const redirect = (to: string) => {
    window.location.href = to;
}
