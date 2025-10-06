import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from "@/components/ui/sidebar"
import { ComponentType } from "react";
import { NavLink } from "react-router";
import Logo from "@/components/logo";

export type SidebarItem = {
  title: string;
  url: string;
  icon: ComponentType<any>;
}

export function AppSidebar({ items, theme }: { items: SidebarItem[], theme: "light" | "dark" | undefined }) {
  return (
    <Sidebar collapsible="icon" variant="sidebar">
      <SidebarHeader>
        <Logo theme={theme} logoType="vertical" className="px-20 sm:hidden" />
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu className="mt-2.5">
              {items.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink
                      to={item.url}
                      className={({ isActive }) => isActive ? "bg-muted text-foreground" : "hover:bg-accent"}
                    >
                      <item.icon />
                      <span>{item.title}</span>
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}

            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarRail />
    </Sidebar>
  )
}
