import 'vitest';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useIsMobile } from '@/hooks/use-mobile';

describe('useIsMobile', () => {
    let matchMediaMock: any;
    let listeners: ((event?: any) => void)[] = [];

    beforeEach(() => {
        listeners = [];
        matchMediaMock = {
            matches: false,
            addEventListener: vi.fn((event: string, handler: (event?: any) => void) => {
                listeners.push(handler);
            }),
            removeEventListener: vi.fn((event: string, handler: (event?: any) => void) => {
                listeners = listeners.filter(l => l !== handler);
            })
        };

        Object.defineProperty(window, 'matchMedia', {
            writable: true,
            configurable: true,
            value: vi.fn(() => matchMediaMock)
        });
    });

    afterEach(() => {
        listeners = [];
    });

    it('returns false for desktop width (>= 768px)', () => {
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 1024
        });

        const { result } = renderHook(() => useIsMobile());
        expect(result.current).toBe(false);
    });

    it('returns true for mobile width (< 768px)', () => {
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 375
        });

        const { result } = renderHook(() => useIsMobile());
        expect(result.current).toBe(true);
    });

    it('returns true for tablet width just below breakpoint (767px)', () => {
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 767
        });

        const { result } = renderHook(() => useIsMobile());
        expect(result.current).toBe(true);
    });

    it('returns false for width at breakpoint (768px)', () => {
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 768
        });

        const { result } = renderHook(() => useIsMobile());
        expect(result.current).toBe(false);
    });

    it('updates when window is resized from desktop to mobile', () => {
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 1024
        });

        const { result } = renderHook(() => useIsMobile());
        expect(result.current).toBe(false);

        // Simulate resize to mobile
        act(() => {
            Object.defineProperty(window, 'innerWidth', {
                writable: true,
                configurable: true,
                value: 375
            });
            listeners.forEach(listener => listener());
        });

        expect(result.current).toBe(true);
    });

    it('updates when window is resized from mobile to desktop', () => {
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 375
        });

        const { result } = renderHook(() => useIsMobile());
        expect(result.current).toBe(true);

        // Simulate resize to desktop
        act(() => {
            Object.defineProperty(window, 'innerWidth', {
                writable: true,
                configurable: true,
                value: 1024
            });
            listeners.forEach(listener => listener());
        });

        expect(result.current).toBe(false);
    });

    it('cleans up event listener on unmount', () => {
        const { unmount } = renderHook(() => useIsMobile());
        
        expect(matchMediaMock.addEventListener).toHaveBeenCalledTimes(1);
        
        unmount();
        
        expect(matchMediaMock.removeEventListener).toHaveBeenCalledTimes(1);
    });

    it('registers matchMedia listener with correct breakpoint', () => {
        renderHook(() => useIsMobile());
        
        expect(window.matchMedia).toHaveBeenCalledWith('(max-width: 767px)');
    });
});

