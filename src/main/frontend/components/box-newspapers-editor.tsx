import {useCallback, useEffect, useMemo, useState} from "react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Switch} from "@/components/ui/switch";
import {Plus, Save, Trash2} from "lucide-react";
import {format, addDays, parseISO, isValid} from "date-fns";
import {nb} from "date-fns/locale";
import HuginTitle from "@/generated/no/nb/bikube/hugin/model/HuginTitle";
import {HuginNewspaperService} from "@/generated/endpoints";
import Newspaper from "@/generated/no/nb/bikube/hugin/model/Newspaper";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {useAddNewspapers} from "@/hooks/use-create-item";

type NewspaperRow = Omit<Newspaper, "date"> & {
    date: string;
    _tmpId: string;
};


export default function BoxNewspapersEditor({title}: { title: HuginTitle }) {
    const activeBox = title?.boxes?.find(b => b.active);
    if (activeBox === undefined) return null;

    const existingDates = useMemo(
        () =>
            (activeBox?.newspapers ?? [])
                .map((n) => (n.date as unknown as string | undefined)?.slice(0, 10))
                .filter(Boolean) as string[],
        [activeBox?.newspapers]
    );

    const [rows, setRows] = useState<NewspaperRow[]>([]);

    useEffect(() => {
        const newspapers = title.boxes.find(b => b.active)?.newspapers;
        if (newspapers && newspapers.length > 0) {
            setRows(newspapers.map(n => ({
                ...n,
                date: (n.date as unknown as string)?.slice(0, 10) ?? "",
                _tmpId: crypto.randomUUID(),
            })));
        }
    }, [title.boxes]);

    if (activeBox === undefined) return null;
    if (title.releasePattern === undefined) return null;

    const isReleaseDay = useCallback(
        (iso: string) => {
            const d = parseISO(iso);
            if (!isValid(d)) return true; // allow manual edits even if parse fails
            // JS: 0=Sun..6=Sat -> Mon=0..Sun=6
            const idx = (d.getDay() + 6) % 7;
            return Number(title.releasePattern![idx] ?? 0) > 0;
        },
        [title.releasePattern]
    );

    const suggestNextDate = useCallback((): string => {
        // start baseline at latestBox.dateFrom ONLY (as requested)
        let probe: string = activeBox.dateFrom!;

        // if we already have any (existing or local), move to last+1
        const all = [...existingDates, ...rows.map((r) => r.date!)].sort();
        if (all.length > 0) {
            const last = all.at(-1)!;
            probe = format(addDays(parseISO(last), 1), "yyyy-MM-dd");
        }

        const any = title.releasePattern!.some((n) => Number(n) > 0);
        if (!any) return probe;

        for (let i = 0; i < 31; i++) {
            if (isReleaseDay(probe)) return probe;
            probe = format(addDays(parseISO(probe), 1), "yyyy-MM-dd");
        }
        return probe;
    }, [activeBox.dateFrom, existingDates, rows, title.releasePattern, isReleaseDay]);

    const suggestNextEditionNumber = useCallback((): string | undefined => {
        // find highest existing edition number (existing or local)
        const allEditions = [
            ...activeBox.newspapers,
            ...rows,
        ]
            .map((n) => Number(n.edition))
            .filter((n) => !isNaN(n));
        if (allEditions.length === 0) return undefined;
        const max = Math.max(...allEditions);
        return (max + 1).toString();
    }, [activeBox.newspapers, rows]);

    const addRow = () => {
        const date = suggestNextDate();
        const editionSuggestion = suggestNextEditionNumber();
        setRows((rs) => [
            ...rs,
            {
                _tmpId: crypto.randomUUID(),
                catalogId: "",
                titleId: title.id!,
                edition: editionSuggestion,
                date,
                received: false,
                username: undefined,
                notes: "",
                box: {id: activeBox.id} as any,
            },
        ]);
    };

    const setRow = (id: string, patch: Partial<NewspaperRow>) =>
        setRows((rs) => rs.map((r) => (r._tmpId === id ? ({...r, ...patch}) : r)));

    const addNewspapers = useAddNewspapers(); // <-- your React Query mutation hook

    const handleSave = async () => {
        if (rows.length === 0) return;
        const payload = rows.map((r) => ({
            catalogId: r.catalogId,
            edition: r.edition?.toString() || "",
            titleId: title.id,
            date: r.date,
            received: r.received || false,
            username: r.username?.trim() || "",
            notes: r.notes?.trim() || "",
            boxId: activeBox.id,
        }));

        const result = await addNewspapers.mutateAsync({items: payload});
        if (Array.isArray(result)) {
            setRows(result.map((n: any) => ({
                ...n,
                date: (n.date as string)?.slice(0, 10) ?? "",
                _tmpId: crypto.randomUUID(),
            })));
        }

    };

    const handleDelete = async (id: string) => {
        const catalogueId = rows.find((r) => r._tmpId === id)?.catalogId;
        await HuginNewspaperService.deleteNewspaper(catalogueId!);
        setRows((rs) => rs.filter((r) => r._tmpId !== id));
    };

    return (
        <div className="w-full space-y-4">
            <div className="flex items-center gap-2">
                <Button type="button" className="bg-primary/20 text-blue-900" onClick={addRow}>
                    Legg til ny utgave <Plus/>
                </Button>
                <Button type="button" className="ml-auto" onClick={handleSave} disabled={addNewspapers.isPending}>
                    Lagre <Save/>
                </Button>
            </div>

            <div className="rounded-xl border bg-white shadow-sm">
                <Table className="w-full table-auto">
                    <TableHeader>
                        <TableRow className="bg-muted/50">
                            <TableHead>Dag</TableHead>
                            <TableHead>Dato</TableHead>
                            <TableHead>Nummer</TableHead>
                            <TableHead>Mottatt</TableHead>
                            <TableHead>Kommentar</TableHead>
                            <TableHead className="w-12"/>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {rows.map((r) => {
                            const d = r.date ? parseISO(r.date) : undefined;
                            const dag = d && isValid(d) ? format(d, "EEEE", {locale: nb}) : "—";
                            return (
                                <TableRow key={r._tmpId}>
                                    <TableCell className="capitalize">{dag}</TableCell>
                                    <TableCell>
                                        <Input
                                            type="date"
                                            value={r.date ?? ""}
                                            onChange={(e) => setRow(r._tmpId, {date: e.target.value.slice(0, 10)})}
                                            className="w-[150px]"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Input
                                            inputMode="numeric"
                                            pattern="[0-9]*"
                                            value={r.edition ?? ""}
                                            onChange={(e) =>
                                                setRow(r._tmpId, {
                                                    edition: e.target.value === "" ? undefined : e.target.value.replace(/\D+/g, ""),
                                                })
                                            }
                                            className="w-[60px]"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex items-center gap-2">
                                            <Switch
                                                checked={!!r.received}
                                                onCheckedChange={(v) => setRow(r._tmpId, {received: v})}
                                            />
                                            <Label className="text-muted-foreground">
                                                {r.received ? "Mottatt" : "Ikke mottatt"}
                                            </Label>
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <Input value={r.notes ?? ""}
                                               onChange={(e) => setRow(r._tmpId, {notes: e.target.value})}/>
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button
                                            type="button"
                                            variant="destructive"
                                            className="h-8 w-8 p-0 rounded-full"
                                            onClick={() => removeRow(r._tmpId)}
                                        >
                                            <Trash2 className="h-4 w-4"/>
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            );
                        })}

                        {rows.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={6}>
                                    <div className="flex items-center justify-between">
                                        <p className="text-sm text-muted-foreground p-2">
                                            Ingen utgaver lagt til.
                                        </p>
                                    </div>
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
