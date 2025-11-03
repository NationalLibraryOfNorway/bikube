import { useQuery } from '@tanstack/react-query';
import {HuginCollectionsService } from '@/generated/endpoints';
import { redirect } from '@/lib/utils';
import {keys} from "@/query/keys";
import {toast} from "sonner";

export function useCatalogueTitles(query: string) {

  const q = useQuery({
    queryKey: keys.catalogueTitles(query),
    enabled: query?.trim().length > 0,
    retry: false,
    queryFn: async () => {
      try {
        return await HuginCollectionsService.findByTitle(query);
      } catch (e: any) {
        if (e?.response?.status === 401 || String(e?.message ?? '').includes('401')) {
          redirect('/bikube/hugin');
          return [];
        }
        throw e;
      }
    },
    select: (arr) => arr ?? [],
  });

  return {
    catalogueTitlesList: q.data ?? [],
    isLoading: q.isLoading,
    search: q.refetch,
  };
}

export function useCatalogueTitle(id: string) {
    const q = useQuery({
        queryKey: keys.catalogueTitle(id),
        retry: false,
        queryFn: async () => {
            try {
                return await HuginCollectionsService.findById(id);
            } catch (e: any) {
                if (e?.response?.status === 401 || String(e?.message ?? '').includes('401')) {
                    redirect('/bikube/hugin');
                    return undefined;
                }
                toast.error("Feil ved søk i katalogen. Vennligst prøv igjen senere. Hvis problemet vedvarer, kontakt brukerstøtte for hjelp.")
                throw e;
            }
        },
    });

    return {
        catalogueTitle: q.data ?? undefined,
        isLoading: q.isLoading,
        search: q.refetch,
    };
}
