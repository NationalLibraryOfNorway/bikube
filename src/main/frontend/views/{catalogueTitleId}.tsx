import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {useParams} from "react-router";
import {useHuginTitle} from "@/hooks/use-hugin-title";
import {ArrowLeft, Edit, LoaderCircle} from "lucide-react";
import {useCatalogueTitle} from "@/hooks/use-catalogue-title";
import {Button} from "@/components/ui/button";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: true,
    title: "Detaljer for katalogtittel",
};

export default function CatalogueTitleView() {
    const {catalogueTitleId} = useParams(); // Item id from url
    const {title, isLoading} = useHuginTitle(Number.parseInt(catalogueTitleId!))
    const {catalogueTitle} = useCatalogueTitle(catalogueTitleId!)

    if (title === null && !isLoading) {
        return (
            <div className="max-w-[50rem] mx-auto">
                <div className="w-full xl:flex xl:justify-center text-4xl mb-10">
                    {catalogueTitle?.name}
                </div>
                <div className="w-full xl:flex xl:justify-center text-lg mb-10">
                    Fant ikke kontakt- og utgivelsesinformasjon for denne tittelen. Ønsker du å legge til?
                </div>
                <div className="w-full xl:flex xl:justify-center text-lg">
                    <Button variant="outline" size="lg" className="mr-auto"><ArrowLeft/>Tilbake</Button>
                    <Button size="lg" >Legg til informasjon<Edit/></Button>
                </div>
            </div>
        )
    }

    if (!title && isLoading) {
        return (
            <div className="w-full xl:flex xl:justify-center">
                <LoaderCircle className="animate-spin"/><p className="text-3xl">Laster...</p>
            </div>
        )
    }

    return (
        <div className="w-full xl:flex xl:justify-center">
            <p className="text-3xl">{title?.vendor}</p>
        </div>
    );
}

