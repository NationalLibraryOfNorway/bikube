import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import { Search } from "lucide-react";
import {useAuth} from "@/context/auth-context";
import Logo from "@/components/logo";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: true,
    title: 'Velg tekstobjekt - AMMO'
};

export default function MainView() {
    const { state } = useAuth()

    return (
        <div className="flex mt-20 flex-col items-center justify-center">
            <Logo className="w-[150px] mb-3" />
            <div className="relative w-80">
                <input
                    type="text"
                    placeholder="Søk etter avistittel..."
                    className="w-full p-3 pr-10 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
                <Search className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            </div>
        </div>
    )
};
