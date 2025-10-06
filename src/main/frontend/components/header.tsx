import { NavLink } from "react-router";
import { Button, HorizontalLayout } from "@vaadin/react-components";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { LogOut, Sun, Moon } from "lucide-react";
import Logo from "@/components/logo";
import { translate, key } from "@vaadin/hilla-react-i18n";

type HeaderProps = {
  theme: "light" | "dark";
  onToggleTheme: () => void;
  user?: { initials?: string; fullName?: string };
  onLogout: () => void;
};

export default function Header({ theme, onToggleTheme, user, onLogout }: HeaderProps) {
  return (
    <div className="w-full xl:flex xl:justify-center">
      <HorizontalLayout
        className="flex max-w-[1280px] xl:w-[1280px] items-center pe-5 pb-5 pt-1 sm:pe-10 h-22"
        theme="spacing padding"
      >
        <SidebarTrigger />

        <NavLink to={"/"}>
          <Logo theme={theme} logoType="horizontal" className="w-[250px] ps-5" />
        </NavLink>

        <Button
          onClick={onToggleTheme}
          className="ml-auto bg-transparent dark:text-white text-black min-w-1 p-2 rounded-full"
        >
          {theme === "dark" ? <Moon /> : <Sun />}
        </Button>

        <HorizontalLayout className="items-center" theme="spacing-xs">
          <Avatar>
            <AvatarFallback className="dark:text-black text-xs bg-gray-300 header-mono">
              {user?.initials}
            </AvatarFallback>
          </Avatar>
          <p className="text-sm !m-0 header-mono hidden sm:inline">{user?.fullName}</p>
        </HorizontalLayout>

        <Button
          className="header-mono bg-transparent text-black p-2 min-w-1"
          onClick={onLogout}
        >
          <HorizontalLayout className="items-center text-purple-500 text-sm">
            <span className="hidden sm:inline">{translate(key`common.logout`)}</span>
            <LogOut className="ms-1" />
          </HorizontalLayout>
        </Button>
      </HorizontalLayout>
    </div>
  );
}

