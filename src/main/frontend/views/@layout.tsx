import {Outlet} from "react-router";
import {AppLayout} from "@vaadin/react-components";
import {AuthProvider, useAuth} from "Frontend/context/auth-context";
import "@/fonts.css";
import "@/globals.css"
import {Toaster} from "@/components/ui/sonner";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {UserSettingsProvider, useUserSettings} from "@/context/user-settings-context";
import {TooltipProvider} from "@radix-ui/react-tooltip";
import Header from "@/components/header";


function MainLayout() {
    const {theme, toggleTheme} = useUserSettings()
    const {logout, state} = useAuth();

    return (<>
        <div className={'flex flex-col h-screen w-full px-5 pb-5'}>
            <Toaster/>
            {<Header
                onLogout={logout}
                onToggleTheme={toggleTheme}
                user={{initials: state.user?.initials, fullName: state.user?.fullName}}
            />}
            <div className="flex-grow flex justify-center pt-6">
                <Outlet/>
            </div>
        </div>
    </>)
}

export default function MainLayoutWithProviders() {
    const queryClient = new QueryClient();

    return (
        <AppLayout>
            <AuthProvider>
                <QueryClientProvider client={queryClient}>
                    <UserSettingsProvider>
                        <TooltipProvider>
                            <MainLayout/>
                        </TooltipProvider>
                    </UserSettingsProvider>
                </QueryClientProvider>
            </AuthProvider>
        </AppLayout>
    );
}
