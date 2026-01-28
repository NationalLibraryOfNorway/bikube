/******************************************************************************
 * Custom entry point for Vaadin Hilla.
 * This file overrides the auto-generated index.tsx in the generated folder.
 ******************************************************************************/

import { createElement } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { router } from 'Frontend/routes.js';  // Use custom routes file

function App() {
    return <RouterProvider router={router} />;
}

const outlet = document.getElementById('outlet')!;
let root = (outlet as any)._root ?? createRoot(outlet);
(outlet as any)._root = root;
root.render(createElement(App));

