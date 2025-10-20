import {useMutation, useQueryClient} from "@tanstack/react-query";
import {toast} from "sonner";
import NewspaperUpsertDto from "@/generated/no/nb/bikube/hugin/model/dto/NewspaperUpsertDto";
import {HuginNewspaperService} from "@/generated/endpoints";
import {keys} from "@/query/keys";

type Args = {
    items: NewspaperUpsertDto[];
};

export function useAddNewspapers() {
    const qc = useQueryClient();

    return useMutation({
        mutationFn: async (args: Args) => {
            await HuginNewspaperService.upsertNewspaper(args.items)
    /*        for (const n of args.items) {

            }*/
        },
        onSuccess: (_data, vars) => {
            toast.success("Utgaver lagret");
            //qc.invalidateQueries({queryKey: });
            //qc.invalidateQueries({queryKey: ["box", vars.boxId]});
        },
        onError: () => toast.error("Klarte ikke å lagre utgaver"),
    });
}
