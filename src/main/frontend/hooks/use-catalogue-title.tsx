import { useQuery } from '@tanstack/react-query';
import {
    useSearchTitle,
    useGetSingleTitle,
    SearchTitleMaterialType,
    GetSingleTitleMaterialType,
} from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';
import { redirect } from '@/lib/utils';
import {keys} from "@/query/keys";
import {toast} from "sonner";

export function useCatalogueTitles(query: string) {
  const queryResult = useSearchTitle(
    { searchTerm: query, materialType: SearchTitleMaterialType.NEWSPAPER },
    {
      query: {
        queryKey: keys.catalogueTitles(query),
        enabled: query?.trim().length > 0,
        retry: false,
        select: (arr) => arr ?? [],
      },
    }
  );

  return {
    catalogueTitlesList: queryResult.data ?? [],
    isLoading: queryResult.isLoading,
    search: queryResult.refetch,
  };
}

export function useCatalogueTitle(id: string) {
    const query = useGetSingleTitle(
        { catalogueId: id, materialType: GetSingleTitleMaterialType.NEWSPAPER },
        {
            query: {
                queryKey: keys.catalogueTitle(id),
                retry: false,
            },
        }
    );

    return {
        catalogueTitle: query.data ?? undefined,
        isLoading: query.isLoading,
        search: query.refetch,
    };
}
