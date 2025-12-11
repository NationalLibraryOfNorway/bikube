import HuginTitle from "@/generated/no/nb/bikube/hugin/model/dbo/HuginTitle";
import {Form, FormikProvider, useFormik} from "formik";
import ContactType from "@/generated/no/nb/bikube/hugin/model/ContactType";
import {Info, MessageCirclePlus, MailPlus, Minus, SaveIcon, Undo} from "lucide-react";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";
import {useSaveHuginTitle} from "@/hooks/use-save-hugin-title";
import {toast} from "sonner";
import {useParams} from "react-router";

type ContactFormValues = {
    id: number;
    vendor: string;
    contactName: string;
    shelf: string;
    notes: string;
    contactInfos: HuginTitle['contactInfos'];
};

type ContactFormField =
    | "vendor"
    | "contactName"
    | "phone"
    | "email"
    | "shelf"
    | "notes";

const ALL_FIELDS: ContactFormField[] = ["vendor", "contactName", "phone", "email", "shelf", "notes"];

export default function ContactForm({title, fields}: {
    title: HuginTitle | null | undefined;
    fields?: ContactFormField[];
}) {
    const {catalogueTitleId} = useParams(); // Item id from url
    const save = useSaveHuginTitle();
    const formik = useFormik({
        enableReinitialize: true,
        initialValues: {
            id: Number.parseInt(catalogueTitleId!),
            vendor: title?.vendor ?? '',
            contactName: title?.contactName ?? '',
            shelf: title?.shelf ?? '',
            notes: title?.notes ?? '',
            contactInfos: title?.contactInfos ?? [],
        },
        onSubmit: async (values: ContactFormValues) => {
            await save.mutateAsync(values)
                .then(() => toast.success("Lagret kontaktinformasjon"))
                .catch(() => toast.error("Noe gikk galt ved lagring av kontaktinformasjon"));
        },

        validateOnChange: true,
    })

    const phoneContacts = formik.values.contactInfos
        .map((ci, i) => [ci, i] as const)
        .filter(([ci]) => ci.contactType === ContactType.phone)
        .map(([, i]) => i);

    const emailContacts = formik.values.contactInfos
        .map((ci, i) => [ci, i] as const)
        .filter(([ci]) => ci.contactType === ContactType.email)
        .map(([, i]) => i);

    const visible = new Set(fields ?? ALL_FIELDS);

    return (
        <FormikProvider value={formik}>
            <Form
                onSubmit={formik.handleSubmit}
                className="w-full max-w-xl space-y-5"
            >
                <h2 className="text-xl font-semibold">Kontaktinformasjon</h2>

                {/* Avleverer (vendor) */}
                {visible.has("vendor") && (
                <div className="space-y-1">
                    <label className="block text-sm font-medium">Avleverer</label>
                    <input
                        className="w-full rounded-lg border p-3 bg-white"
                        placeholder=""
                        {...formik.getFieldProps('vendor')}
                    />
                </div>)}

                {/* Navn */}
                {visible.has("contactName") && (
                <div className="space-y-1">
                    <label className="block text-sm font-medium">Navn</label>
                    <input
                        className="w-full rounded-lg border p-3 bg-white"
                        {...formik.getFieldProps('contactName')}
                    />
                </div>)}

                {/* Telefon (+/-) */}
                {visible.has("phone") && (
                <div className="space-y-2">
                    <label className="block text-sm font-medium">Telefon</label>
                    {phoneContacts.map((idx) => (
                        <div key={`phone-${idx}`} className="relative">
                            <input
                                className="w-full rounded-lg border py-3 ps-3 pr-10 bg-white"
                                name={`contactInfos[${idx}].contactValue`}
                                value={formik.values.contactInfos[idx]?.contactValue ?? ""}
                                onChange={formik.handleChange}
                                type="tel"
                            />
                            <Button
                                type="button"
                                className="absolute right-2 top-1/2 -translate-y-1/2 h-6 w-6 rounded-full p-0"
                                onClick={() =>
                                    formik.setFieldValue(
                                        "contactInfos",
                                        formik.values.contactInfos.filter((_, i) => i !== idx)
                                    )
                                }
                            >
                                <Minus className="h-3.5 w-3.5"/>
                            </Button>
                        </div>
                    ))}
                    <Button
                        type="button"
                        size="lg"
                        variant="secondary"
                        onClick={() =>
                            formik.setFieldValue("contactInfos", [
                                ...formik.values.contactInfos,
                                {contactType: ContactType.phone, contactValue: ""},
                            ])
                        }
                    >
                        <MessageCirclePlus/>
                        Legg til telefon
                    </Button>
                </div>)}

                {/* E-post (+/-) */}
                {visible.has("email") && (
                <div className="space-y-2">

                    <label className="block text-sm font-medium">E-post</label>
                    {emailContacts.map((idx) => (
                        <div key={`email-${idx}`} className="relative">
                            <input
                                className="w-full rounded-lg border py-3 ps-3 pr-10 bg-white"
                                name={`contactInfos[${idx}].contactValue`}
                                value={formik.values.contactInfos[idx]?.contactValue ?? ""}  // 👈 always a string
                                onChange={formik.handleChange}
                                type="email"
                            />
                            <Button
                                type="button"
                                className="absolute right-2 top-1/2 -translate-y-1/2 h-6 w-6 rounded-full p-0"
                                onClick={() =>
                                    formik.setFieldValue(
                                        "contactInfos",
                                        formik.values.contactInfos.filter((_, i) => i !== idx)
                                    )
                                }
                            >
                                <Minus className="h-3.5 w-3.5"/>
                            </Button>
                        </div>
                    ))}
                    <Button
                        type="button"
                        size="lg"
                        variant="secondary"
                        onClick={() =>
                            formik.setFieldValue("contactInfos", [
                                ...formik.values.contactInfos,
                                {contactType: ContactType.email, contactValue: ""},
                            ])
                        }
                    >
                        <MailPlus />
                        Legg til e-post
                    </Button>
                </div>)}

                {/* Hyllesignatur */}
                {visible.has("shelf") && (
                <div className="space-y-1">
                    <label className="flex items-center gap-1 text-sm font-medium">
                        Hyllesignatur
                        <Tooltip>
                            <TooltipTrigger>
                                <Info className="h-4 w-4 opacity-60"/>
                            </TooltipTrigger>
                            <TooltipContent>
                                Plassering av avis i paternoster.
                            </TooltipContent>
                        </Tooltip>

                    </label>
                    <input
                        className="w-full rounded-lg border p-3 bg-white"
                        {...formik.getFieldProps('shelf')}
                    />
                </div>)}

                {/* Merknad/kommentar */}
                {visible.has("notes") && (
                <div className="space-y-1">
                    <label className="flex items-center gap-1 text-sm font-medium">
                        Merknad/kommentar
                        <Tooltip>
                            <TooltipTrigger>
                                <Info className="h-4 w-4 opacity-60"/>
                            </TooltipTrigger>
                            <TooltipContent>
                                Kommentarer blir kun liggende i Hugin og lagres ikke i katalogen.
                            </TooltipContent>
                        </Tooltip>
                    </label>
                    <textarea
                        rows={4}
                        className="w-full rounded-lg border p-3 bg-white"
                        {...formik.getFieldProps('notes')}
                    />
                </div>)}

                <div className="flex gap-3">
                    <Button size="lg" type="submit">
                        Lagre
                        <SaveIcon/>
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        size="lg"
                        onClick={() => {
                            formik.resetForm()
                            toast.info('Endringer angret')
                        }}
                    >
                        Angre
                        <Undo/>
                    </Button>
                </div>
            </Form>
        </FormikProvider>
    )
}
