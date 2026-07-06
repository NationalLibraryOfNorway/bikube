import 'vitest';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import { expect } from 'vitest';
import * as matchers from '@testing-library/jest-dom/matchers';
import { server } from './server';

expect.extend(matchers);

beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }));
afterEach(() => {
    cleanup();
    server.resetHandlers();
});
afterAll(() => server.close());
