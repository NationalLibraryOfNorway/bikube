import { configureAuth } from '@vaadin/hilla-react-auth';
import { AuthContextService } from 'Frontend/generated/endpoints';

// Configure auth to use `UserInfoService.getUserInfo`
const auth = configureAuth(AuthContextService.getUserInfo);

export const useAuth = auth.useAuth;
export const AuthProvider = auth.AuthProvider;