import {useQueryClient} from '@tanstack/react-query';
import {useGetTitle, type HuginTitle} from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';
import {keys} from "@/query/keys";

export function useHuginTitle(titleId: number) {
    const queryClient = useQueryClient();
    const query = useGetTitle(titleId, {
        query: {
            retry: false,
            enabled: Number.isFinite(titleId),
            initialData: () => queryClient.getQueryData<HuginTitle | null>(keys.huginTitle(titleId)) ?? undefined,
        }
    });

    return {
        title: query.data ?? null,
        isLoading: query.isLoading,
        isError: query.isError,
        error: query.error,
    };
}
