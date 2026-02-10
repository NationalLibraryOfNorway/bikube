/******************************************************************************
 * Custom routes configuration for Vaadin Hilla.
 * This file overrides the auto-generated routes.tsx in the generated folder.
 ******************************************************************************/
import { RouterConfigurationBuilder } from '@vaadin/hilla-file-router/runtime.js';
import Flow from 'Frontend/generated/flow/Flow';
import fileRoutes from 'Frontend/generated/file-routes';

export const { router, routes } = new RouterConfigurationBuilder()
    .withFileRoutes(fileRoutes)
    .withFallback(Flow)
    // Pass the login path to protect() so it knows where to redirect
    // unauthenticated users
    .protect('/login')
    .build();

