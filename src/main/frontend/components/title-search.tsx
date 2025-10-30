import {Command, CommandEmpty, CommandInput, CommandItem, CommandList} from "@/components/ui/command";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {LoaderCircle} from "lucide-react";
import Title from "@/generated/no/nb/bikube/api/core/model/Title";
import {isActive} from "@/lib/utils";
import {clsx} from "clsx";
import {Badge} from "@/components/ui/badge";
import React, {useMemo, useRef, useState} from "react";
import {useCatalogueTitles} from "@/hooks/use-catalogue-title";
import {useNavigate} from "react-router";
import {useQueryClient} from "@tanstack/react-query";
import {keys} from "@/query/keys";

export default function TitleSearch({ className }:{ className?: string }) {
    const [term, setTerm] = useState("")
    const [open, setOpen] = useState(false)
    const contentRef = useRef<HTMLDivElement>(null)
    const {catalogueTitlesList, isLoading } = useCatalogueTitles(term.trim());
    const navigate = useNavigate();

    const handleSelect = (item: Title) => {
        qc.setQueryData( keys.catalogueTitle(item.catalogueId), item)
        navigate(`${item.catalogueId}`);
        setOpen(false);
    };
    const qc = useQueryClient();
    const rows: Title[] = useMemo(() => catalogueTitlesList as Title[], [catalogueTitlesList]);

    return (
        <div className="w-[25rem] max-w-[90vw]">
            <Command className="w-full" shouldFilter={false}>
                <Popover open={open} onOpenChange={setOpen}>
                    <PopoverTrigger asChild>
                        <div className={clsx(
                            className,
                            "border-2 border-gray-200 rounded-full",
                        )}>
                            <CommandInput
                                value={term}
                                onValueChange={(v) => {
                                    setTerm(v)
                                    if (v.length === 0) setOpen(false)
                                    if (!open) setOpen(true)
                                }}
                                onFocus={() => setOpen(true)}
                                placeholder="Søk etter avistittel…"
                                onKeyDown={(e) => {
                                    if (e.key === "Escape") setOpen(false)
                                }}
                            />
                        </div>
                    </PopoverTrigger>

                    <PopoverContent
                        onOpenAutoFocus={(e) => e.preventDefault()}
                        className="my-2 rounded-lg w-[23rem] max-w-[90vw] p-0"
                    >
                        <CommandList onMouseDown={(e) => e.preventDefault()}>
                            {!isLoading && <CommandEmpty>Ingen treff.</CommandEmpty>}
                            {isLoading &&
                                <div className="p-2">
                                    <LoaderCircle className="animate-spin mx-auto size-10 text-gray-300"/>
                                </div>}

                            {rows.map((item: Title) => {
                                const active = isActive(item.endDate)
                                return (
                                    <CommandItem
                                        className={clsx(
                                            "p-3 my-1 mx-2",
                                            "data-[selected=true]:bg-gray-300",
                                            active ? "bg-green-100" : "",
                                        )}
                                        value={item.catalogueId}
                                        key={item.catalogueId}
                                        onSelect={() => handleSelect(item)}
                                    >
                                        {item.name}
                                        {active &&
                                            <Badge className="rounded-full bg-green-500 ml-auto">
                                                Aktiv
                                            </Badge>
                                        }
                                    </CommandItem>
                                )
                            })}

                        </CommandList>
                    </PopoverContent>
                </Popover>
            </Command>
        </div>
    )
}
