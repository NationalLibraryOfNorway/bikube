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
        },
        onSuccess: (_data, vars) => {
            toast.success("Utgaver lagret");
            qc.invalidateQueries({queryKey: keys.huginTitle(vars.items[0].titleId)});
        },
        onError: () => toast.error("Klarte ikke å lagre utgaver"),
    });
}
