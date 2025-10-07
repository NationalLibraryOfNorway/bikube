import { Outlet } from "react-router";
import { AppLayout } from "@vaadin/react-components";
import { AuthProvider, useAuth } from "Frontend/context/auth-context";
import { useEffect } from "react";
import { HomeIcon, User, Settings } from "lucide-react";
import "@/fonts.css";
import "@/globals.css"
import { Toaster } from "@/components/ui/sonner";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { SidebarProvider } from "@/components/ui/sidebar";
import { AppSidebar, SidebarItem } from "@/components/app-sidebar";
import { UserSettingsProvider, useUserSettings } from "@/context/user-settings-context";
import { TooltipProvider } from "@radix-ui/react-tooltip";
import { i18n, key, translate } from '@vaadin/hilla-react-i18n';
import Header from "@/components/header";


function MainLayout() {
    const { theme, toggleTheme } = useUserSettings()
    const { logout, state } = useAuth();
    const { language } = useUserSettings();

    useEffect(() => {
        i18n.configure(
            { language: language },
        );
    }, [language]);

    const sidebarMenuItems: SidebarItem[] = [
        {
            title: translate(key`menu.title.home`),
            icon: HomeIcon,
            url: "/",
        },
        {
            title: translate(key`menu.title.userprofile`),
            icon: User,
            url: "/profile",
        },
        {
            title: translate(key`menu.title.settings`),
            icon: Settings,
            url: "/settings",
        },
    ]

    return (<>
        <AppSidebar theme={theme} items={sidebarMenuItems} />
        <div className={'flex flex-col h-screen w-full px-5 pb-5'}>
            <Toaster />
            {/*<Header
                theme={theme}
                onLogout={logout}
                onToggleTheme={toggleTheme}
                user={{ initials: state.user?.initials, fullName: state.user?.fullName }}
            />*/}
            <Outlet />
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
                            <SidebarProvider defaultOpen={false}>
                                <MainLayout />
                            </SidebarProvider>
                        </TooltipProvider>
                    </UserSettingsProvider>
                </QueryClientProvider>
            </AuthProvider>
        </AppLayout>
    );
}
