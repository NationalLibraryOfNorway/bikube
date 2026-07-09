import {useQueryClient} from "@tanstack/react-query";
import {toast} from "sonner";
import {useUpsertNewspapers, type NewspaperUpsertDto} from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';
import {keys} from "@/query/keys";

type Args = {
    items: NewspaperUpsertDto[];
};

export function useAddNewspapers() {
    const queryClient = useQueryClient();
    const mutation = useUpsertNewspapers();

    return {
        ...mutation,
        mutateAsync: async (args: Args) => {
            await mutation.mutateAsync({ data: args.items });
            toast.success("Utgaver lagret");
            queryClient.invalidateQueries({queryKey: keys.huginTitle(args.items[0].titleId!)});
        },
    };
}
