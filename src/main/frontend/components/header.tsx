import {NavLink, useLocation} from "react-router";
import {HorizontalLayout} from "@vaadin/react-components";
import {Avatar, AvatarFallback} from "@/components/ui/avatar";
import {LogOut, Sun, Moon} from "lucide-react";
import Logo from "@/components/logo";
import TitleSearch from "@/components/title-search";
import {Button} from "@/components/ui/button";

type HeaderProps = {
    onToggleTheme: () => void;
    user?: { initials?: string; fullName?: string };
    onLogout: () => void;
};

export default function Header({onToggleTheme, user, onLogout}: HeaderProps) {
    const location = useLocation();

    return (
        <div className="w-full xl:flex xl:justify-center">
            <HorizontalLayout
                className="flex justify-between max-w-[1280px] xl:w-[1280px] items-center pe-5 pb-5 pt-1 sm:pe-10 h-22"
                theme="spacing padding"
            >
                <NavLink to="/">
                    <div className="flex items-center gap-2">
                        <Logo className="w-[45px]"/>
                        <span className={"text-2xl"}>Hugin</span>
                    </div>
                </NavLink>

                {location.pathname != "/" && <TitleSearch className="p-1"/>}

                <div className="flex items-center gap-4">
                    <HorizontalLayout className="items-center" theme="spacing-xs">
                        <Avatar>
                            <AvatarFallback className="dark:text-black text-xs bg-gray-300 header-mono">
                                {user?.initials}
                            </AvatarFallback>
                        </Avatar>
                        <p className="text-sm !m-0 header-mono hidden sm:inline">{user?.fullName}</p>
                    </HorizontalLayout>

                    <Button onClick={onLogout} variant="secondary">
                        <span className="hidden sm:inline text-xs">Logg ut</span>
                        <LogOut className="ms-1"/>
                    </Button>
                </div>
            </HorizontalLayout>
        </div>

    );
}

