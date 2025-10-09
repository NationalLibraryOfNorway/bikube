import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {useNavigate, useParams} from "react-router";
import {useHuginTitle} from "@/hooks/use-hugin-title";
import {Button} from "@/components/ui/button";
import {ArrowLeft, CircleQuestionMark, LoaderCircle, SaveIcon} from "lucide-react";
import {useCatalogueTitle} from "@/hooks/use-catalogue-title";
import ReleasePatternForm from "@/components/release-pattern-form";
import {Field, Form, Formik} from "formik";
import {Tooltip} from "@/components/ui/tooltip";
import {TextArea} from "@vaadin/react-components";
import {toast} from "sonner";
import ContactForm from "@/components/contact-form";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: true,
    title: "Legg til utgivelsesinformasjon for katalogtittel",
};

export default function CatalogueTitleCreateView() {
    const {catalogueTitleId} = useParams(); // Item id from url
    const {catalogueTitle} = useCatalogueTitle(catalogueTitleId!);
    const {title} = useHuginTitle(Number.parseInt(catalogueTitleId!))
    const navigate = useNavigate();

    console.log(title);

    return (
        <div className="flex w-9/12 flex-col max-w-screen-lg items-start">
            <Button variant="secondary" onClick={() => navigate(-1)} className="font-light mb-8">
                <ArrowLeft/>
                Tilbake til titteloversikt
            </Button>

            <div className="flex items-center gap-4 mb-5">
                <h1 className="text-4xl font-medium">{catalogueTitle?.name}</h1>
                <p className="mt-1.5">({catalogueTitle?.catalogueId})</p>
            </div>

            <div className="flex flex-row gap-8 mb-10">
                <div>
                    <ContactForm title={title}></ContactForm>
                </div>
                <div>
                </div>
                <div>
                </div>

            </div>
        </div>
    )
}
