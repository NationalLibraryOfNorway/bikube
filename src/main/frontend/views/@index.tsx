import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {useState} from "react";
import {ChevronFirst, ChevronLast} from "lucide-react";
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious
} from "@/components/ui/pagination";
import {useAuth} from "@/context/auth-context";
import {ScrollArea, ScrollBar} from "@/components/ui/scroll-area";
import {Button} from "../components/ui/button";
import {getPagesButtonsList} from "../lib/utils";

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
    const { state } = useAuth()

    return (
        <div className="flex min-h-screen flex-col">

        </div >
    )
};
