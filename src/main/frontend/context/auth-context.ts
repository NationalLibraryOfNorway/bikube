import { configureAuth } from '@vaadin/hilla-react-auth';
import { AuthContextService } from 'Frontend/generated/endpoints';

// Configure auth to use `AuthContextService.getUserInfo`
const auth = configureAuth(AuthContextService.getUserInfo);

// Custom logout function - calls backend to invalidate session and get Keycloak logout URL
async function logout() {
    try {
        // Call backend logout which invalidates session and returns Keycloak logout URL
        const keycloakLogoutUrl = await AuthContextService.logout();
        // Redirect to Keycloak logout
        window.location.href = keycloakLogoutUrl;
    } catch (error) {
        console.error('Logout failed:', error);
        // Fallback - just redirect to root
        window.location.href = '/bikube/';
    }
}

export const useAuth = () => {
    const authState = auth.useAuth();
    return {
        ...authState,
        logout, // Override with custom logout
    };
};
export const AuthProvider = auth.AuthProvider;