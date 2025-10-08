import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {useParams} from "react-router";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: true,
    title: "Detaljer for katalogtittel",
};

export default function CatalogueTitleView() {
    const { catalogueTitleId } = useParams(); // Item id from url

    return (
        <div className="w-full xl:flex xl:justify-center">
        <p>Hei</p>
        </div>
    );
}

