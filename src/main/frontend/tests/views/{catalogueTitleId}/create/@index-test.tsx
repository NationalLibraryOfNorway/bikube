// filepath: /home/gardgu/projects/bikube/src/main/frontend/tests/views/{catalogueTitleId}/create/@index-test.tsx
/// <reference types="vitest" />
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import CatalogueTitleCreateView from '@/views/{catalogueTitleId}/create/@index';
import userEvent from '@testing-library/user-event';

// Mock the hooks
const mockUseHuginTitle = vi.fn();
const mockUseCatalogueTitle = vi.fn();
const mockNavigate = vi.fn();

vi.mock('@/hooks/use-hugin-title', () => ({
    useHuginTitle: () => mockUseHuginTitle()
}));

vi.mock('@/hooks/use-catalogue-title', () => ({
    useCatalogueTitle: () => mockUseCatalogueTitle()
}));

vi.mock('react-router', async () => {
    const actual = await vi.importActual('react-router');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
        useParams: () => ({ catalogueTitleId: '456' })
    };
});

// Mock child components
vi.mock('@/components/contact-form', () => ({
    default: ({ title }: any) => (
        <div data-testid="contact-form">
            Contact Form {title ? `for ${title.id}` : 'without title'}
        </div>
    )
}));

vi.mock('@/components/release-pattern-form', () => ({
    default: ({ title }: any) => (
        <div data-testid="release-pattern-form">
            Release Pattern Form {title ? `for ${title.id}` : 'without title'}
        </div>
    )
}));

vi.mock('@/components/ui/button', () => ({
    Button: ({ children, onClick, variant, className, ...props }: any) => (
        <button onClick={onClick} className={className} data-variant={variant} {...props}>
            {children}
        </button>
    )
}));

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn()
    }
}));

const mockCatalogueTitle = {
    catalogueId: '456',
    name: 'New Test Newspaper'
};

const mockTitle = {
    id: '456',
    name: 'New Test Newspaper',
    shelf: 'B-456',
    vendor: 'Test Vendor',
    contactName: 'John Doe',
    phone: '12345678',
    email: 'test@example.com'
};

describe('CatalogueTitleCreateView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderComponent = () => {
        return render(
            <MemoryRouter initialEntries={['/456/create']}>
                <Routes>
                    <Route path="/:catalogueTitleId/create" element={<CatalogueTitleCreateView />} />
                </Routes>
            </MemoryRouter>
        );
    };

    it('renders the page title and catalogue information', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        expect(screen.getByText('New Test Newspaper')).toBeInTheDocument();
        expect(screen.getByText('(456)')).toBeInTheDocument();
    });

    it('renders the back button', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        const backButton = screen.getByRole('button', { name: /Tilbake til titteloversikt/i });
        expect(backButton).toBeInTheDocument();
    });

    it('navigates back when back button is clicked', async () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();
        const user = userEvent.setup();

        const backButton = screen.getByRole('button', { name: /Tilbake til titteloversikt/i });
        await user.click(backButton);

        expect(mockNavigate).toHaveBeenCalledWith(-1);
    });

    it('renders ContactForm component', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        expect(screen.getByTestId('contact-form')).toBeInTheDocument();
    });

    it('renders ReleasePatternForm component', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        expect(screen.getByTestId('release-pattern-form')).toBeInTheDocument();
    });

    it('passes title to ContactForm when title exists', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: mockTitle });

        renderComponent();

        const contactForm = screen.getByTestId('contact-form');
        expect(contactForm).toHaveTextContent('for 456');
    });

    it('passes title to ReleasePatternForm when title exists', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: mockTitle });

        renderComponent();

        const releasePatternForm = screen.getByTestId('release-pattern-form');
        expect(releasePatternForm).toHaveTextContent('for 456');
    });

    it('renders forms without title when title does not exist', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        const contactForm = screen.getByTestId('contact-form');
        expect(contactForm).toHaveTextContent('without title');

        const releasePatternForm = screen.getByTestId('release-pattern-form');
        expect(releasePatternForm).toHaveTextContent('without title');
    });

    it('has the correct layout structure', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        const { container } = renderComponent();

        const mainWrapper = container.querySelector('.flex.w-9\\/12');
        expect(mainWrapper).toBeInTheDocument();

        const formsWrapper = container.querySelector('.flex.w-full.flex-row.gap-20');
        expect(formsWrapper).toBeInTheDocument();
    });

    it('displays catalogue ID in parentheses', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        const catalogueIdElement = screen.getByText(/\(456\)/);
        expect(catalogueIdElement).toBeInTheDocument();
    });

    it('calls useHuginTitle with the correct catalogueTitleId', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        expect(mockUseHuginTitle).toHaveBeenCalled();
    });

    it('calls useCatalogueTitle with the correct catalogueTitleId', () => {
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });
        mockUseHuginTitle.mockReturnValue({ title: null });

        renderComponent();

        expect(mockUseCatalogueTitle).toHaveBeenCalled();
    });
});

