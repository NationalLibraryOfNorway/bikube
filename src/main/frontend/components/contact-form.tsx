import HuginTitle from "@/generated/no/nb/bikube/hugin/model/HuginTitle";
import {FieldArray, Form, Formik, FormikProvider, useFormik} from "formik";
import ContactType from "@/generated/no/nb/bikube/hugin/model/ContactInfo/ContactType";
import ContactInfo from "@/generated/no/nb/bikube/hugin/model/ContactInfo";
import {Info, MailPlus, MessageCircle, MessageCirclePlus, MessageCircleX, Minus, SaveIcon, Undo} from "lucide-react";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";

type ContactFormValues = {
    vendor: string;
    contactName: string;
    shelf: string;
    notes: string;
    contactInfos: HuginTitle['contactInfos'];
};

export default function ContactForm({title, onSubmit}: {
    title: HuginTitle | null | undefined;
    onSubmit?: (values: ContactFormValues) => void;
}) {

    const formik = useFormik({
        enableReinitialize: true,
        initialValues: {
            vendor: title?.vendor ?? '',
            contactName: title?.contactName ?? '',
            shelf: title?.shelf ?? '',
            notes: title?.notes ?? '',
            contactInfos: title?.contactInfos ?? [],
        },
        onSubmit: (values: ContactFormValues) => {
        },
        validateOnChange: true,
    })
    const {values, getFieldProps, handleSubmit, setFieldValue, resetForm} = formik;

    const addContact = (type: ContactType) =>
        setFieldValue("contactInfos", [
            ...values.contactInfos,
            {contactType: type, contactValue: ""} as ContactInfo,
        ]);

    const removeContactAt = (absoluteIndex: number) =>
        setFieldValue(
            "contactInfos",
            values.contactInfos.filter((_, i) => i !== absoluteIndex)
        );

    const phoneContacts = formik.values.contactInfos
        .map((ci, i) => [ci, i] as const)
        .filter(([ci]) => ci.contactType === ContactType.phone)
        .map(([, i]) => i);

    const emailContacts = formik.values.contactInfos
        .map((ci, i) => [ci, i] as const)
        .filter(([ci]) => ci.contactType === ContactType.email)
        .map(([, i]) => i);


    return (
        <FormikProvider value={formik}>
            <Form
                onSubmit={formik.handleSubmit}
                className="w-full max-w-xl space-y-5"
            >
                <h2 className="text-xl font-semibold">Kontaktinformasjon</h2>

                {/* Avleverer (vendor) */}
                <div className="space-y-1">
                    <label className="block text-sm font-medium">Avleverer</label>
                    <input
                        className="w-full rounded-lg border p-3"
                        placeholder=""
                        {...formik.getFieldProps('vendor')}
                    />
                </div>

                {/* Navn */}
                <div className="space-y-1">
                    <label className="block text-sm font-medium">Navn</label>
                    <input
                        className="w-full rounded-lg border p-3"
                        {...formik.getFieldProps('contactName')}
                    />
                </div>

                {/* Telefon (+/-) */}
                <div className="space-y-2">
                    <label className="block text-sm font-medium">Telefon</label>
                    <FieldArray
                        name="phones"
                        render={(arrayHelpers) => (
                            <div className="space-y-2">
                                {phoneContacts.map((i) => (
                                    <div key={i} className="flex items-center gap-2">
                                        <input
                                            className="flex-1 rounded-lg border p-3"
                                            {...formik.getFieldProps(`phones[${i}]`)}
                                        />
                                        <Button
                                            onClick={() => removeContactAt(i)}
                                            size="icon"
                                            className="h-6 w-6 rounded-full p-0"
                                        >
                                            <Minus/>
                                        </Button>
                                    </div>
                                ))}
                                <Button
                                    size="lg"
                                    variant="secondary"
                                    onClick={() => addContact(ContactType.phone)}
                                >
                                    <MessageCirclePlus/>
                                    Legg til telefon
                                </Button>

                            </div>
                        )}
                    />
                </div>

                {/* E-post (+/-) */}
                <div className="space-y-2">
                    <label className="block text-sm font-medium">E-post</label>
                    <FieldArray
                        name="emails"
                        render={(arrayHelpers) => (
                            <div className="space-y-2">
                                {emailContacts.map((i) => (
                                    <div key={i} className="flex items-center gap-2">
                                        <input
                                            className="flex-1 rounded-lg border p-3"
                                            type="email"
                                            {...formik.getFieldProps(`emails[${i}]`)}
                                        />
                                        <Button
                                            className="h-6 w-6 rounded-full p-0"
                                            onClick={() => removeContactAt(i)}
                                        >
                                            <Minus/>
                                        </Button>
                                    </div>
                                ))}
                                <Button
                                    size="lg"
                                    variant="secondary"
                                    onClick={() => addContact(ContactType.email)}
                                >
                                    <MailPlus/>
                                    Legg til e-post
                                </Button>
                            </div>
                        )}
                    />
                </div>

                {/* Hyllesignatur */}
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
                        className="w-full rounded-lg border p-3"
                        {...formik.getFieldProps('shelf')}
                    />
                </div>

                {/* Merknad/kommentar */}
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
                        className="w-full rounded-lg border p-3"
                        {...formik.getFieldProps('notes')}
                    />
                </div>

                <div className="flex gap-3">
                    <Button size="lg" type="submit">
                        Lagre
                        <SaveIcon/>
                    </Button>
                    <Button variant="outline" size="lg" onClick={() => formik.resetForm()}>
                        Avbryt
                        <Undo/>
                    </Button>
                </div>
            </Form>
        </FormikProvider>
    )
}
