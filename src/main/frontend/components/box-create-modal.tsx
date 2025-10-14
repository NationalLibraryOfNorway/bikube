import * as React from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Box as BoxIcon, SaveIcon } from "lucide-react";
import BoxCreateForm from "@/components/box-create-form";

export default function CreateBoxModal() {
    const [open, setOpen] = React.useState(false);

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="secondary">
                    Ny eske <BoxIcon />
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-lg"> {/* optional width */}
                <DialogHeader>
                    <DialogTitle>Registrer en ny eske</DialogTitle>
                </DialogHeader>

                {/* Form lives inside the modal */}
                <BoxCreateForm onSuccess={() => setOpen(false)} />
            </DialogContent>
        </Dialog>
    );
}
