import { useQuery } from '@tanstack/react-query';
import {HuginCollectionsService } from '@/generated/endpoints';
import { redirect } from '@/lib/utils';
import {keys} from "@/query/keys";
import {toast} from "sonner";

export function useCatalogueTitles(query: string) {

  const queryResult = useQuery({
    queryKey: keys.catalogueTitles(query),
    enabled: query?.trim().length > 0,
    retry: false,
    queryFn: async () => {
      try {
        return await HuginCollectionsService.findByTitle(query);
      } catch (e: unknown) {
        if ((e as { response?: { status?: number } })?.response?.status === 401 ||
            (e instanceof Error && e.message.includes('401'))) {
          redirect('/bikube/hugin');
          return [];
        }
        throw e;
      }
    },
    select: (arr) => arr ?? [],
  });

  return {
    catalogueTitlesList: queryResult.data ?? [],
    isLoading: queryResult.isLoading,
    search: queryResult.refetch,
  };
}

export function useCatalogueTitle(id: string) {
    const query = useQuery({
        queryKey: keys.catalogueTitle(id),
        retry: false,
        queryFn: async () => {
            try {
                return await HuginCollectionsService.findById(id);
            } catch (e: unknown) {
                if ((e as { response?: { status?: number } })?.response?.status === 401 ||
                    (e instanceof Error && e.message.includes('401'))) {
                    redirect('/bikube/hugin');
                    return undefined;
                }
                toast.error("Feil ved søk i katalogen. Vennligst prøv igjen senere. Hvis problemet vedvarer, kontakt brukerstøtte for hjelp.")
                throw e;
            }
        },
    });

    return {
        catalogueTitle: query.data ?? undefined,
        isLoading: query.isLoading,
        search: query.refetch,
    };
}
