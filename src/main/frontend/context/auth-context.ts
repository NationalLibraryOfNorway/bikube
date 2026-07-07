// TODO(TT-migration): Replace with axios-based auth using /bikube/api/auth/me
// The @vaadin/hilla-react-auth configureAuth and AuthContextService.getUserInfo
// have been removed as part of the Hilla → WebFlux migration.
// Implement a new useAuth hook that calls GET /bikube/api/auth/me via axios.

export function logout() {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/bikube/logout';
    document.body.appendChild(form);
    form.submit();
}
