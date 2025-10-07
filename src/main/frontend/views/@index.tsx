import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {useState} from "react";
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
        <div className="flex flex-col items-center justify-center mt-20">
            <Logo className={"w-[150px] mb-5"}/>
            <input
                type="text"
                placeholder="Søk etter avistittel..."
                className="w-80 p-3 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
        </div>

    )
};
