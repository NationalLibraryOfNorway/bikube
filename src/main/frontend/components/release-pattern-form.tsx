import {Table, TableBody, TableCell, TableHeader, TableRow, TableHead} from '@/components/ui/table';
import NumberInputWithButtons from "@/components/number-input";
import HuginTitle from "@/generated/no/nb/bikube/hugin/model/HuginTitle";
import { Form, FormikProvider, useFormik } from "formik";
import { Button } from "@/components/ui/button";
import { SaveIcon, Undo } from "lucide-react";
import { toast } from "sonner";
import { useSaveHuginTitle } from "@/hooks/use-save-hugin-title";
import {useParams} from "react-router";

type ReleasePatternFormValues = {
    id: number;
    releasePattern: number[];
};

export default function ReleasePatternForm({ title }: { title: HuginTitle | null | undefined }) {
    const { catalogueTitleId } = useParams();
    const save = useSaveHuginTitle();

    const initial = (title?.releasePattern as number[] | undefined) ?? [0, 0, 0, 0, 0, 0, 0];
    const daysOfWeek = ["Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"];

    const formik = useFormik<ReleasePatternFormValues>({
        enableReinitialize: true,
        initialValues: {
            id: Number.parseInt(catalogueTitleId!),
            releasePattern: initial,
        },
        onSubmit: async (values) => {
            try {
                await save.mutateAsync(values);
                toast.success("Utgivelsesmønster lagret");
            } catch {
                toast.error("Klarte ikke å lagre utgivelsesmønster");
            }
        },
        validateOnChange: true,
    });

    const { values, setFieldValue } = formik;

    const setDayValue = (index: number, raw: number | string) => {
        const n = Number(raw);
        const next = [...values.releasePattern];
        next[index] = Math.trunc(Number(n));
        setFieldValue("releasePattern", next);
    };

    return (
        <FormikProvider value={formik}>
            <Form onSubmit={formik.handleSubmit} className="w-full max-w-xl space-y-5">
                <h2 className="text-xl font-semibold">Utgivelsesmønster</h2>

                <Table className="table-fixed text-left mb-5" aria-labelledby="releaseTable">
                    <TableHeader>
                        <TableRow>
                            <TableHead>Dag</TableHead>
                            <TableHead>Antall</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {daysOfWeek.map((day, index) => (
                            <TableRow key={index} className="text-left">
                                <TableCell className="text-lg p-0">{day}</TableCell>
                                <TableCell className="m-2.5 py-0 pr-0 w-full">
                                    <NumberInputWithButtons
                                        value={values.releasePattern[index] ?? 0}
                                        onChange={(v: number) => setDayValue(index, v)}
                                        min={0}
                                        step={1}
                                        className="input-number-style"
                                    />
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>

                <div className="flex gap-3">
                    <Button size="lg" type="submit">
                        Lagre
                        <SaveIcon />
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        size="lg"
                        onClick={() => {
                            if (formik.dirty) {
                                formik.resetForm();
                                toast.info("Endringer forkastet");
                            } else {
                                toast("Ingen endringer å forkaste");
                            }
                        }}
                    >
                        Angre
                        <Undo />
                    </Button>
                </div>
            </Form>
        </FormikProvider>
    );
}
