// filepath: /home/gardgu/projects/bikube/src/main/frontend/tests/views/{catalogueTitleId}/@index-test.tsx
import 'vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import CatalogueTitleView from '@/views/{catalogueTitleId}/@index';
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
        useParams: () => ({ catalogueTitleId: '123' })
    };
});

// Mock child components
vi.mock('@/components/title-comment-form', () => ({
    default: () => <div data-testid="title-comment-form">Title Comment Form</div>
}));

vi.mock('@/components/contact-form', () => ({
    default: () => <div data-testid="contact-form">Contact Form</div>
}));

vi.mock('@/components/release-pattern-form', () => ({
    default: () => <div data-testid="release-pattern-form">Release Pattern Form</div>
}));

vi.mock('@/components/box-create-modal', () => ({
    default: () => <div data-testid="box-create-modal">Box Create Modal</div>
}));

vi.mock('@/components/box-newspapers-editor', () => ({
    default: () => <div data-testid="box-newspapers-editor">Box Newspapers Editor</div>
}));

vi.mock('@/components/ui/button', () => ({
    Button: ({ children, onClick, className, ...props }: any) => (
        <button onClick={onClick} className={className} {...props}>{children}</button>
    )
}));

vi.mock('@/components/ui/tooltip', () => ({
    Tooltip: ({ children }: any) => <>{children}</>,
    TooltipTrigger: ({ children }: any) => <>{children}</>,
    TooltipContent: ({ children }: any) => <span>{children}</span>
}));

const mockTitle = {
    id: '123',
    name: 'Test Newspaper',
    shelf: 'A-123',
    boxes: [
        {
            id: 'BOX-1',
            dateFrom: '2024-01-01',
            active: true
        }
    ]
};

const mockCatalogueTitle = {
    name: 'Test Newspaper from Catalogue'
};

describe('CatalogueTitleView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderComponent = () => {
        return render(
            <MemoryRouter initialEntries={['/123']}>
                <Routes>
                    <Route path="/:catalogueTitleId" element={<CatalogueTitleView />} />
                </Routes>
            </MemoryRouter>
        );
    };

    it('shows loading state when data is being fetched', () => {
        mockUseHuginTitle.mockReturnValue({ title: null, isLoading: true });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: null });

        const { container } = renderComponent();

        expect(screen.getByText('Laster...')).toBeInTheDocument();
        const spinner = container.querySelector('.animate-spin');
        expect(spinner).toBeInTheDocument();
    });

    it('shows create form option when title does not exist', async () => {
        mockUseHuginTitle.mockReturnValue({ title: null, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();

        expect(screen.getByText('Test Newspaper from Catalogue')).toBeInTheDocument();
        expect(screen.getByText(/Fant ikke kontakt- og utgivelsesinformasjon/)).toBeInTheDocument();

        const createButton = screen.getByRole('button', { name: /Legg til informasjon/i });
        expect(createButton).toBeInTheDocument();

        const backButton = screen.getByRole('button', { name: /Tilbake/i });
        expect(backButton).toBeInTheDocument();
    });

    it('navigates to create page when create button is clicked', async () => {
        mockUseHuginTitle.mockReturnValue({ title: null, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();
        const user = userEvent.setup();

        const createButton = screen.getByRole('button', { name: /Legg til informasjon/i });
        await user.click(createButton);

        expect(mockNavigate).toHaveBeenCalledWith('create');
    });

    it('navigates back when back button is clicked', async () => {
        mockUseHuginTitle.mockReturnValue({ title: null, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();
        const user = userEvent.setup();

        const backButton = screen.getByRole('button', { name: /Tilbake/i });
        await user.click(backButton);

        expect(mockNavigate).toHaveBeenCalledWith('/');
    });

    it('displays title information when title exists', () => {
        mockUseHuginTitle.mockReturnValue({ title: mockTitle, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();

        expect(screen.getByText('Test Newspaper from Catalogue')).toBeInTheDocument();
        expect(screen.getByText('123')).toBeInTheDocument();
        expect(screen.getByText(/A-123/)).toBeInTheDocument();
    });

    it('displays active box information when title has boxes', () => {
        mockUseHuginTitle.mockReturnValue({ title: mockTitle, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();

        expect(screen.getByText(/Aktiv eske:/)).toBeInTheDocument();
        expect(screen.getByText(/BOX-1/)).toBeInTheDocument();
        expect(screen.getByText(/2024-01-01/)).toBeInTheDocument();
    });

    it('shows message when title has no boxes', () => {
        const titleWithoutBoxes = { ...mockTitle, boxes: [] };
        mockUseHuginTitle.mockReturnValue({ title: titleWithoutBoxes, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();

        expect(screen.getByText(/Ingen eske registrert/)).toBeInTheDocument();
    });

    it('renders all form components when title exists', () => {
        mockUseHuginTitle.mockReturnValue({ title: mockTitle, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();

        expect(screen.getByTestId('title-comment-form')).toBeInTheDocument();
        expect(screen.getByTestId('contact-form')).toBeInTheDocument();
        expect(screen.getByTestId('release-pattern-form')).toBeInTheDocument();
        expect(screen.getByTestId('box-create-modal')).toBeInTheDocument();
        expect(screen.getByTestId('box-newspapers-editor')).toBeInTheDocument();
    });

    it('displays external link to catalogue', () => {
        mockUseHuginTitle.mockReturnValue({ title: mockTitle, isLoading: false });
        mockUseCatalogueTitle.mockReturnValue({ catalogueTitle: mockCatalogueTitle });

        renderComponent();

        const link = screen.getByRole('link', { name: /123/i });
        expect(link).toHaveAttribute('href', expect.stringContaining('collections.stage.nb.no'));
        expect(link).toHaveAttribute('target', '_blank');
    });
});

