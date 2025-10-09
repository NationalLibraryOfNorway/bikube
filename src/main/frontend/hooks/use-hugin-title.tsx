import {useQuery, useQueryClient} from '@tanstack/react-query';
import {HuginNewspaperService} from '@/generated/endpoints';
import HuginTitle from "@/generated/no/nb/bikube/hugin/model/HuginTitle";
import {keys} from "@/query/keys";

export function useHuginTitle(titleId: number) {
    const qc = useQueryClient();
    const q = useQuery<HuginTitle | null>({
        queryKey: keys.huginTitle(titleId),
        retry: false,
        enabled: Number.isFinite(titleId),
        queryFn: async () => await HuginNewspaperService.getTitle(titleId) ?? null,
        initialData: () => qc.getQueryData<HuginTitle | null>(keys.huginTitle(titleId)) ?? null
    });

    return {
        title: q.data ?? null,
        isLoading: q.isLoading,
        isError: q.isError,
        error: q.error,
    };
}
