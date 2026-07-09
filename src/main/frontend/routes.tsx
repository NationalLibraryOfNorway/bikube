import { createBrowserRouter } from 'react-router';
import MainLayoutWithProviders from '@/views/@layout';
import MainView from '@/views/@index';
import Login from '@/views/login';
import { lazy } from 'react';

const CatalogueTitleView = lazy(() => import('@/views/{catalogueTitleId}/@index'));
const CatalogueTitleCreateView = lazy(() => import('@/views/{catalogueTitleId}/create/@index'));

export const router = createBrowserRouter(
    [
        {
            path: '/',
            element: <MainLayoutWithProviders />,
            children: [
                { index: true, element: <MainView /> },
                { path: 'login', element: <Login /> },
                { path: ':catalogueTitleId', element: <CatalogueTitleView /> },
                { path: ':catalogueTitleId/create', element: <CatalogueTitleCreateView /> },
            ],
        },
    ],
    { basename: '/bikube/hugin' }
);
