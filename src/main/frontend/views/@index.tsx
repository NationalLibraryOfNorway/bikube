import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {useAuth} from "@/context/auth-context";
import Logo from "@/components/logo";
import React, {useMemo, useRef, useState} from "react";
import {useCatalogueTitle} from "@/hooks/use-catalogue-title";
import {
    Command,
    CommandEmpty,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/ui/command";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {LoaderCircle} from "lucide-react";
import {Badge} from "@/components/ui/badge";
import Title from "@/generated/no/nb/bikube/api/core/model/Title";
import {isActive} from "@/lib/utils";
import {clsx} from "clsx";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: true,
    title: 'Velg tekstobjekt - AMMO'
};

export default function MainView() {
    const {state} = useAuth()
    const [term, setTerm] = useState("")
    const [open, setOpen] = useState(false)
    const contentRef = useRef<HTMLDivElement>(null)
    const {options, isLoading, selected, setSelected} = useCatalogueTitle({
        query: term.trim(),
    });

    const handleSelect = (item: Title) => {
        setSelected(item as any);
        // TODO: navigate or do something meaningful:
    };
    const rows: Title[] = useMemo(() => options as Title[], [options]);

    // Close when clicking outside the popover content
    function handleBlur(e: React.FocusEvent<HTMLInputElement>) {
        // Wait a tick so clicks in the popover can run first
        setTimeout(() => {
            const active = document.activeElement as HTMLElement | null
            if (!contentRef.current?.contains(active ?? null)) {
                setOpen(false)
            }
        }, 0)
    }

    return (
        <div className="flex mt-20 flex-col items-center justify-center">
            <Logo className="w-[150px] mb-5"/>
            <div className="w-[25rem] max-w-[90vw]">
                <Command className="w-full" shouldFilter={false}>
                    <Popover open={open} onOpenChange={setOpen}>
                        <PopoverTrigger asChild>
                            <div className=" p-3 border-2 border-gray-200 rounded-full">
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
                            ref={contentRef}
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
        </div>
    )
};
