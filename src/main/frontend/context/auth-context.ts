import { configureAuth } from '@vaadin/hilla-react-auth';
import { AuthContextService } from 'Frontend/generated/endpoints';

// Configure auth to use `AuthContextService.getUserInfo`
const auth = configureAuth(AuthContextService.getUserInfo);

// Logout by POSTing to Spring's logout endpoint.
// Spring handles session invalidation and redirects to Keycloak's logout endpoint,
// which then redirects back to the app.
function logout() {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/bikube/logout';
    document.body.appendChild(form);
    form.submit();
}

export const useAuth = () => {
    const authState = auth.useAuth();
    return {
        ...authState,
        logout, // Override with custom logout
    };
};
export const AuthProvider = auth.AuthProvider;
