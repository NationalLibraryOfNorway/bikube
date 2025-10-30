/// <reference types="vitest" />
import { describe, it, expect, vi } from 'vitest';
import { render } from '@testing-library/react';
import Login from '@/views/login';

vi.mock('@/lib/utils', async (orig) => {
    const actual = await orig<typeof import('@/lib/utils')>();
    return { ...actual, redirect: vi.fn() };
});
import { redirect } from '@/lib/utils';

describe('Home / login', () => {
    it('Should redirect to hugin root', () => {
        render(<Login />);
        expect(redirect).toHaveBeenCalledWith('/bikube/hugin');
    });
});
