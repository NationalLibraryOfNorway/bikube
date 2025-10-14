import {useMemo, useState} from "react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Switch} from "@/components/ui/switch";
import {toast} from "sonner";
import {Plus} from "lucide-react";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {HuginNewspaperService} from "@/generated/endpoints";
import {format, addDays, parseISO} from "date-fns";
import {nb} from "date-fns/locale";
import Box from "@/generated/no/nb/bikube/hugin/model/Box";

type NewspaperRow = {
    date: string;
    edition?: string;
    received: boolean;
    notes?: string;
};

export default function BoxNewspapersEditor({
                                                box,
                                                existingDates = [],
                                            }: {
    box: Box;
    existingDates?: string[];
}) {
    const qc = useQueryClient();

    // Compute next date: last existing + 1 day, else startDate
    const nextDate: string = useMemo(() => {
        if (existingDates.length === 0) return box.dateFrom!;
        const last = existingDates.slice().sort().at(-1)!;
        return format(addDays(parseISO(last), 1), "yyyy-MM-dd");
    }, [existingDates, box.dateFrom]);

    const [row, setRow] = useState<NewspaperRow>({
        date: nextDate,
        edition: undefined,
        received: false,
        notes: "",
    });

    // Keep date in sync when nextDate changes (e.g., after add)
    if (row.date !== nextDate) {
        // naive sync; fine for this controlled widget
        // eslint-disable-next-line react/no-direct-mutation-state
        row.date = nextDate;
    }

    const addNewspaper = useMutation({
        mutationFn: async (payload: NewspaperRow) => {
            // ADDED: backend will also auto-place date if omitted;
            // we send date to show the day label before submit.
            return await HuginNewspaperService.addNewspaper({
                titleId: box.title!.id,         // allows authorization/ownership checks server-side
                boxId: box.id,
                date: payload.date,
                edition: payload.edition,
                received: payload.received,
                notes: payload.notes?.trim() || undefined,
            });
        },
        onSuccess: (saved) => {
            toast.success(`Utgave ${format(parseISO(saved.date!), "d. MMMM yyyy", {locale: nb})} lagt til`);
            // Invalidate box query so existingDates refresh
            qc.invalidateQueries({queryKey: ["box", box.id]});
            // Prepare next row (date will sync from nextDate when existingDates reload)
            setRow((r) => ({...r, number: undefined, received: false, comment: ""}));
        },
        onError: () => toast.error("Klarte ikke å legge til avisutgave"),
    });

    const weekday = format(parseISO(row.date), "EEEE", {locale: nb}); // e.g., "mandag"

    return (
        <div className="space-y-3">
            <div className="flex flex-wrap items-end gap-3">
                <div>
                    <Label>Neste dato</Label>
                    <div className="px-3 py-2 border rounded-md bg-muted/20">
                        <span className="font-mono">{row.date}</span>{" "}
                        <span className="text-muted-foreground">({weekday})</span>
                    </div>
                </div>

                <div className="w-32">
                    <Label htmlFor="number">Nummer</Label>
                    <Input
                        id="number"
                        inputMode="numeric"
                        pattern="[0-9]*"
                        value={row.edition ?? ""}
                        onChange={(e) => {
                            const n = e.target.value === "" ? undefined : Math.trunc(Number(e.target.value));
                            if (Number.isNaN(n as any)) return;
                            setRow((r) => ({...r, number: n}));
                        }}
                        placeholder="—"
                    />
                </div>

                <div className="flex items-center gap-2">
                    <Switch
                        id="received"
                        checked={row.received}
                        onCheckedChange={(v) => setRow((r) => ({...r, received: v}))}
                    />
                    <Label htmlFor="received">Mottatt</Label>
                </div>

                <div className="min-w-[16rem] flex-1">
                    <Label htmlFor="comment">Kommentar</Label>
                    <Input
                        id="comment"
                        value={row.notes ?? ""}
                        onChange={(e) => setRow((r) => ({...r, comment: e.target.value}))}
                        placeholder="Valgfri kommentar"
                    />
                </div>

                <Button onClick={() => addNewspaper.mutate(row)} disabled={addNewspaper.isPending}>
                    Legg til <Plus/>
                </Button>
            </div>
        </div>
    );
}
