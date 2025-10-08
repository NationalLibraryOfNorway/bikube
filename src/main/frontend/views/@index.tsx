import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import Logo from "@/components/logo";
import TitleSearch from "@/components/title-search";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: true,
    title: 'Velg tekstobjekt - AMMO'
};

export default function MainView() {

    return (
        <div className="flex mt-20 flex-col items-center justify-center">
            <Logo className="w-[150px] mb-5"/>
            <TitleSearch className="p-3" />
        </div>
    )
};
