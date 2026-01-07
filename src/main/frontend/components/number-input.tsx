import {Minus, Plus} from "lucide-react";
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
        <div className="my-1">
            <div className="gap-1 align-middle inline-flex items-center">
                <Button
                    type="button"
                    onClick={dec}
                    size="icon"
                    className="h-7 w-7 p-0 rounded-full"
                >
                    <Minus />
                </Button>

                <div
                    className="min-w-12 text-center rounded-full border px-2 py-2 bg-white"
                    aria-live="polite"
                    aria-atomic="true"
                >
                    {Number.isFinite(value) ? value : 0}
                </div>


                <Button
                    size="icon"
                    type="button"
                    onClick={inc}
                    className="h-7 w-7 p-0 rounded-full"
                >
                    <Plus />
                </Button>
            </div>
        </div>
    );
}
