import {CircleMinus, CirclePlus} from "lucide-react";
import {Button} from "@/components/ui/button";

export type NumberInputWithButtonsProps = {
    value: number;
    onChange: (next: number) => void;
    min?: number;
    max?: number;
    step?: number;
    className?: string;
};

export default function NumberInputWithButtons({
   value,
   onChange,
   min,
   max,
   step = 1,
   className,
}: NumberInputWithButtonsProps) {
    const inc = () => {
        const next = (value ?? 0) + step;
        onChange(max !== undefined ? Math.min(next, max) : next);
    };

    const dec = () => {
        const next = (value ?? 0) - step;
        onChange(min !== undefined ? Math.max(next, min) : next);
    };

    return (
        <div className="my-1.5">
            <div className="flex flex-row gap-1">
                <Button
                    type="button"
                    onClick={dec}
                    size="icon"
                    className="rounded-full"
                >
                    <CircleMinus />
                </Button>

                <div
                    className="min-w-12 text-center rounded-full border px-3 py-2 "
                    aria-live="polite"
                    aria-atomic="true"
                >
                    {Number.isFinite(value) ? value : 0}
                </div>


                <Button
                    size="icon"
                    type="button"
                    onClick={inc}
                    className="rounded-full"
                >
                    <CirclePlus />
                </Button>
            </div>
        </div>
    );
}
