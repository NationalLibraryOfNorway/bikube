import HuginTitle from "@/generated/no/nb/bikube/hugin/model/HuginTitle";
import { Form, FormikProvider, useFormik } from "formik";
import { Button } from "@/components/ui/button";
import { SaveIcon, Undo } from "lucide-react";
import { toast } from "sonner";
import { useParams } from "react-router";
import { useSaveHuginTitle } from "@/hooks/use-save-hugin-title";

type FormValues = {
    id: number;
    notes: string;
};

export default function TitleCommentForm({ title }: { title: HuginTitle | null | undefined }) {
    const { catalogueTitleId } = useParams();
    const save = useSaveHuginTitle();

    const id = Number.parseInt(catalogueTitleId ?? "", 10);
    if (!Number.isFinite(id)) return null;

    const formik = useFormik<FormValues>({
        enableReinitialize: true,
        initialValues: {
            id,
            notes: title?.notes ?? "",
        },
        validateOnChange: true,
        validate: (v) => {
            const errors: Partial<Record<keyof FormValues, string>> = {};
            if (v.notes.trim().length === 0) {
                errors.notes = "Legg til kommentar";
            }
            return errors;
        },
        onSubmit: async (values, helpers) => {
            try {
                const payload = { id: values.id, notes: values.notes.trim() };
                await save.mutateAsync(payload);
                toast.success("Kommentar lagret");
                helpers.setSubmitting(false);
            } catch {
                toast.error("Klarte ikke å lagre kommentar");
                helpers.setSubmitting(false);
            }
        },
    });

    const { handleSubmit, getFieldProps, resetForm, isSubmitting, dirty } = formik;

    return (
        <FormikProvider value={formik}>
            <Form onSubmit={handleSubmit} className="w-full max-w-xl space-y-4">
                <div className="space-y-1">
                    <textarea
                        rows={4}
                        className="w-full rounded-lg border p-3 bg-white"
                        {...getFieldProps("notes")}
                    />
                </div>

                <div className="flex gap-3">
                    <Button size="lg" type="submit" disabled={isSubmitting}>
                        Lagre
                        <SaveIcon />
                    </Button>

                    <Button
                        type="button"
                        variant="outline"
                        size="lg"
                        onClick={() => {
                            if (dirty) {
                                resetForm();
                                toast.info("Endringer forkastet");
                            } else {
                                toast("Ingen endringer å forkaste");
                            }
                        }}
                    >
                        Avbryt
                        <Undo />
                    </Button>
                </div>
            </Form>
        </FormikProvider>
    );
}
