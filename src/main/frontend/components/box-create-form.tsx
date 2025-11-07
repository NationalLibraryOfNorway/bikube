import {Form, FormikProvider, useFormik} from "formik";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {toast} from "sonner";
import {SaveIcon} from "lucide-react";
import {useParams} from "react-router";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {HuginNewspaperService} from "@/generated/endpoints";
import {Calendar} from "@/components/ui/calendar";
import {format, isValid, parseISO} from "date-fns";
import {nb} from "date-fns/locale";

type CreateBoxFormValues = {
    titleId: number;
    boxId: string;
    dateFrom: string;
};

export default function BoxCreateForm({onSuccess}: { onSuccess?: () => void }) {
    const {catalogueTitleId} = useParams();
    const queryClient = useQueryClient();

    const titleId = Number.parseInt(catalogueTitleId ?? "", 10);
    if (!Number.isFinite(titleId)) return null;

    const createBox = useMutation({
        mutationFn: async (payload: CreateBoxFormValues) => {
            return await HuginNewspaperService.createBox({
                titleId: payload.titleId,
                id: payload.boxId,
                dateFrom: payload.dateFrom,
            });
        },
        onSuccess: (box) => {
            toast.success(`Eske '${box.id}' opprettet`);
            queryClient.invalidateQueries({queryKey: ["huginTitle", titleId]});
            onSuccess?.();
        },
        onError: () => toast.error("Klarte ikke å opprette eske"),
    });

    const formik = useFormik<CreateBoxFormValues>({
        enableReinitialize: true,
        initialValues: {
            titleId,
            boxId: "",
            dateFrom: new Date().toISOString().slice(0, 10), // today
        },
        onSubmit: async (values) => createBox.mutateAsync(values),
    });

    return (
        <FormikProvider value={formik}>
            <Form onSubmit={formik.handleSubmit} className="w-full max-w-xl space-y-4">
                <div className="space-y-1">
                    <Label htmlFor="boxId">Eske ID</Label>
                    <Input id="boxId" {...formik.getFieldProps("boxId")} required/>
                </div>

                <div className="space-y-1">
                    <Label htmlFor="startDate">Fra dato</Label>

                    <Calendar
                        className="border border-1 rounded-md"
                        locale={nb}
                        showOutsideDays={true}
                        captionLayout="dropdown"
                        fixedWeeks={true}
                        mode="single"
                        selected={(() => {
                            const v = formik.values.dateFrom;
                            const d = v ? parseISO(v) : undefined;
                            return d && isValid(d) ? d : undefined;
                        })()}
                        onSelect={(date) => {
                            // store as string yyyy-MM-dd in Formik
                            const value = date ? format(date, "yyyy-MM-dd") : "";
                            formik.setFieldValue("dateFrom", value);
                            formik.setFieldTouched("dateFrom", true, false);
                        }}
                    />

                    {/* show validation error */}
                    {formik.touched.dateFrom && formik.errors.dateFrom && (
                        <p className="text-sm text-red-600">{formik.errors.dateFrom}</p>
                    )}
                </div>

                <Button type="submit" disabled={createBox.isPending}>
                    Lagre <SaveIcon/>
                </Button>
                <Button type="button" variant="ghost" className="float-end"
                        onClick={() => onSuccess?.()}>Avbryt</Button>
            </Form>
        </FormikProvider>
    );
}
