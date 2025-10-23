import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {NavLink, useNavigate, useParams} from "react-router";
import {useHuginTitle} from "@/hooks/use-hugin-title";
import {ArrowLeft, Edit, ExternalLink, LoaderCircle} from "lucide-react";
import {useCatalogueTitle} from "@/hooks/use-catalogue-title";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";
import TitleCommentForm from "@/components/title-comment-form";
import ContactForm from "@/components/contact-form";
import ReleasePatternForm from "@/components/release-pattern-form";
import BoxCreateModal from "@/components/box-create-modal";
import Box from "@/generated/no/nb/bikube/hugin/model/Box";
import BoxNewspapersEditor from "@/components/box-newspapers-editor";

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
    const navigate = useNavigate();
    const hasBoxes = Boolean(title?.boxes?.length);
    const activeBox: Box | undefined = title?.boxes?.find(b => b.active);

    if (title === null && !isLoading) {
        return (
            <div className="max-w-[50rem] mx-auto">
                <div className="w-full flex justify-center text-4xl mb-10">
                    {catalogueTitle?.name}
                </div>
                <div className="w-full flex justify-center text-lg">
                    Fant ikke kontakt- og utgivelsesinformasjon for denne tittelen. Ønsker du å legge til?
                </div>
                <div className="w-full flex justify-center text-lg relative mt-20">
                    <Button
                        variant="secondary"
                        size="lg"
                        className="absolute left-0 bottom-0"
                        onClick={() => navigate("/")}
                    >
                        <ArrowLeft/>Tilbake
                    </Button>
                    <Button
                        size="lg"
                        className="absolute right-0 bottom-0"
                        onClick={() => navigate('create')}
                    >
                        Legg til informasjon<Edit/>
                    </Button>
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
        <div className="max-w-7xl w-full">
            <div className="flex flex-row gap-5">
                <div className="flex flex-col gap-y-5 w-full">
                    <div className="flex flex-col gap-y-2 border-1 p-5 bg-gray-50 rounded-lg">
                        <p className="text-4xl font-medium">
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <NavLink to={'create'}
                                             className={"inline-flex whitespace-nowrap gap-2 items-center"}>
                                        {catalogueTitle?.name}
                                        <Button size="icon" className="p-0 m-0">
                                            <Edit/>
                                        </Button>
                                    </NavLink>
                                </TooltipTrigger>
                                <TooltipContent>Rediger tittelinformasjon</TooltipContent>
                            </Tooltip>
                        </p>
                        <p className="inline-flex whitespace-nowrap gap-2">
                            <span className="font-semibold">Katalog ID:</span>
                            <NavLink
                                to={`https://collections.stage.nb.no/collections/link/xplus/textscatalogue/${title?.id}`}
                                target="_blank"
                                rel="noreferrer"
                                className="inline-flex items-center gap-1 text-blue-600 hover:underline"
                            >
                                {title?.id}
                                <ExternalLink size={16}/>
                            </NavLink>
                        </p>
                        <p className="text-xl">
                            <span className="font-semibold">Hyllesignatur:</span> {title?.shelf}
                        </p>
                    </div>

                    <div className="flex flex-col w-full gap-y-2 border-1 p-5 bg-gray-50 rounded-lg">
                        <div className="flex items-center">
                            {!hasBoxes && (
                                <p className="text-center me-3">Ingen eske registrert, legg til eske for å legge til
                                    avisutgaver</p>
                            )}
                            {hasBoxes && (
                                <p className="text-xl">
                                    <span className="font-semibold">
                                        Aktiv eske:
                                    </span>
                                    <span className="mx-3">
                                        {activeBox?.id} (fra {activeBox?.dateFrom})
                                    </span>
                                </p>
                            )}
                            <BoxCreateModal/>
                        </div>
                    </div>
                    <div className="flex w-full flex-col gap-y-2 border-1 p-5 bg-gray-50 rounded-lg">
                        <BoxNewspapersEditor title={title!}/>
                    </div>
                </div>

                <div className="flex flex-col gap-y-5">
                    <div className="p-5 border-1 bg-gray-50 rounded-lg">
                        <p className="whitespace-nowrap font-semibold mb-3">Merknad/kommentar på tittel:</p>
                        <TitleCommentForm title={title}/>
                    </div>
                    <div className="p-5 border-1 bg-gray-50 rounded-lg">
                        <ContactForm title={title!} fields={["vendor", "contactName", "phone", "email"]}/>
                    </div>
                    <div className="p-5 border-1 bg-gray-50 rounded-lg">
                        <ReleasePatternForm title={title}/>
                    </div>
                </div>
            </div>
        </div>
    )
        ;
}

