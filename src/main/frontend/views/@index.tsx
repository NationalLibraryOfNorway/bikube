import { ViewConfig } from "@vaadin/hilla-file-router/types.js";
import { useEffect, useState } from "react";
import { Bird, ChevronFirst, ChevronLast, LoaderCircle, RouteOff } from "lucide-react";
import { Pagination, PaginationContent, PaginationEllipsis, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from "@/components/ui/pagination";
import { useUserSettings } from "@/context/user-settings-context";
import { TextItemCard } from "@/views/_text-item-card";
import { useAuth } from "@/context/auth-context";
import { usePerItemData } from "@/hooks/useItemImage";
import { useItemsPage } from "@/hooks/useItemPage";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { key, translate } from "@vaadin/hilla-react-i18n";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "../components/ui/button";
import { getPagesButtonsList } from "../lib/utils";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: true,
    title: 'Velg tekstobjekt - AMMO'
};

export default function MainView() {
    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const { thumbnailDimensions, paginationSize } = useUserSettings()
    const { state } = useAuth()

    const { items, totalPages: tp, isLoading, error } = useItemsPage(page, paginationSize);
    const { images, imgErrors, imgLoading } = usePerItemData(items, thumbnailDimensions.height, state.user!);

    useEffect(() => setTotalPages(tp), [tp]);

    return (
        <div className="flex min-h-screen flex-col">
            <div className="flex-1 px-0 pb-10">
                <ScrollArea className="h-full">
                    <div className="flex flex-wrap gap-5 gap-y-8">
                        {!isLoading && !error && items?.map((item) => {
                            const id = String(item!.id);
                            return (
                                <TextItemCard
                                    key={id}
                                    textItem={item!}
                                    width={thumbnailDimensions.width}
                                    height={thumbnailDimensions.height}
                                    iconSize={thumbnailDimensions.iconSize}
                                    src={images[id] ?? undefined}
                                    error={imgErrors[id] ?? false}
                                    loadingImage={imgLoading[id]}
                                />
                            );
                        })}
                        {isLoading && !error && /* Siden laster */(
                            <>
                                {Array.from({ length: paginationSize }).map((_, i) => (
                                    <div key={i} style={{ width: thumbnailDimensions.width, height: thumbnailDimensions.height }}>
                                        <Skeleton className="w-full h-full rounded-md" />
                                    </div>
                                ))}
                                <div className="flex-col flex items-center w-full text-gray-500 absolute">
                                    <LoaderCircle size={80} className="animate-spin mt-10 mb-5" />
                                    <p>{translate(key`itemId.objects.loading`)}</p>
                                </div>
                            </>
                        )}
                        {!isLoading && items.length === 0 && !error && /* Ingen tekstobjekt funnet */ (
                            <div className="flex-col flex items-center w-full text-gray-500">
                                <Bird size={80} className="animate-bounce mt-10 mb-5" />
                                <p>{translate(key`itemId.objects.none.found`)}</p>
                            </div>
                        )}
                        {error && !isLoading && /* Feil ved lasting */(
                            <div className="flex-col flex items-center w-full text-gray-500">
                                <RouteOff size={80} className="animate-shake mt-10 mb-5" />
                                <p>{translate(key`itemId.objects.error.loading`)}</p>
                            </div>
                        )}
                    </div>
                    <ScrollBar orientation="vertical" />
                </ScrollArea>
            </div >
            <div className="sticky bottom-0 py-2 z-10 border-t bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                <Pagination>
                    <PaginationContent>
                        <PaginationItem>
                            <Button
                                className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                                variant="ghost"
                                onClick={() => setPage(0)}
                            >
                                <ChevronFirst />
                            </Button>
                        </PaginationItem>
                        <PaginationItem>
                            <PaginationPrevious
                                onClick={() => setPage((p) => Math.max(p - 1, 0))}
                                className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>

                        {getPagesButtonsList(page, totalPages).map((e) => {
                            return (e === 'ellipsis' ?
                                <PaginationItem>
                                    <PaginationEllipsis />
                                </PaginationItem>
                                :
                                <PaginationItem key={e}>
                                    <PaginationLink
                                        isActive={e === page + 1}
                                        className={e === page + 1 ? "pointer-events-none" : "cursor-pointer"}
                                        onClick={() => { if (typeof e === "number") setPage(e - 1) }}
                                    >{e}
                                    </PaginationLink>
                                </PaginationItem>
                            )
                        })}

                        <PaginationItem>
                            <PaginationNext
                                onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                                className={page === totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                        <PaginationItem>
                            <Button
                                className={page === totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                                variant="ghost"
                                onClick={() => setPage(totalPages - 1)}
                            >
                                <ChevronLast />
                            </Button>
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            </div>
        </div >
    )
};
