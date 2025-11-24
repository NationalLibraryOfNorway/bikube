import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CatalogueService } from '@/generated/endpoints';
import type TextItem from '@/generated/no/nb/papi/textdb/model/db/TextItem';
import type CatalogueTitle from '@/generated/no/nb/papi/ammo/model/CatalogueTitle';
import { getResolvedMetadata } from '@/lib/metadata-utils';
import { redirect } from '@/lib/utils';
import MaterialType from "@/generated/no/nb/papi/common/enums/MaterialType";

type CatalogueTitleParams = {
  materialType?: MaterialType,
  date: string,
  query: string,
  selectBestMatch?: boolean,
}

export function useCatalogueTitle({
  materialType,
  date,
  query,
  selectBestMatch = false,
}: CatalogueTitleParams) {
  const [selected, setSelected] = useState<CatalogueTitle | undefined>(undefined);

  const q = useQuery({
    queryKey: ['catalogue', query, materialType, date],
    enabled: query?.trim().length > 0 && !!materialType,
    retry: false,
    queryFn: async () => {
      if(!materialType || !query) return [];
      try {
        return await CatalogueService.findByTitleAndMaterialType(query, materialType!, selectBestMatch, date);
      } catch (e: any) {
        if (e?.response?.status === 401 || String(e?.message ?? '').includes('401')) {
          redirect('/papi/ammo');
          return [];
        }
        throw e;
      }
    },
    select: (arr) => arr ?? [],
  });

  // Pick first as default once data arrives (don’t override user choice)
  useEffect(() => {
    if (!selected && q.data && q.data.length > 0) {
      setSelected(q.data[0]);
    }
  }, [q.data]); // eslint-disable-line react-hooks/exhaustive-deps

  return {
    options: q.data ?? [],
    isLoading: q.isLoading,
    selected,
    setSelected,
    search: q.refetch,
  };
}
