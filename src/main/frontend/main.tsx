import { createElement } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { router } from './routes'

const outlet = document.getElementById('outlet')!
createRoot(outlet).render(createElement(RouterProvider, { router }))
