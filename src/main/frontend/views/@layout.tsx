import {Outlet} from "react-router";
import {logout} from "@/context/auth-context";
import "@/fonts.css";
import "@/globals.css"
import {Toaster} from "@/components/ui/sonner";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {UserSettingsProvider, useUserSettings} from "@/context/user-settings-context";
import {TooltipProvider} from "@radix-ui/react-tooltip";
import Header from "@/components/header";
import {useGetUserInfo} from "@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger";


function MainLayout() {
    const {toggleTheme} = useUserSettings()
    const {data: user} = useGetUserInfo()

    const initials = [user?.firstName, user?.lastName]
        .filter(Boolean)
        .map(s => s![0])
        .join('')

    return (<>
        <div className={'flex flex-col h-screen w-full px-5 pb-2'}>
            <Toaster/>
            <Header
                onLogout={logout}
                onToggleTheme={toggleTheme}
                user={{initials, fullName: [user?.firstName, user?.lastName].filter(Boolean).join(' ')}}
            />
            <div className="flex-grow flex justify-center pt-6">
                <Outlet/>
            </div>
            <div className="font-light flex flex-col w-full text-center">Nasjonalbiblioteket © 2025</div>
        </div>
    </>)
}

export default function MainLayoutWithProviders() {
    const queryClient = new QueryClient();

    return (
        <div>
            <QueryClientProvider client={queryClient}>
                <UserSettingsProvider>
                    <TooltipProvider>
                        <MainLayout/>
                    </TooltipProvider>
                </UserSettingsProvider>
            </QueryClientProvider>
        </div>
    );
}
