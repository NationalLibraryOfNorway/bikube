import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {HuginCollectionsService } from '@/generated/endpoints';
import { redirect } from '@/lib/utils';

type CatalogueTitleParams = {
  query: string,
}

export function useCatalogueTitle({
  query,
}: CatalogueTitleParams) {
  const [selected, setSelected] = useState<null | undefined>(undefined);

  const q = useQuery({
    queryKey: ['title', query ],
    enabled: query?.trim().length > 0,
    retry: false,
    queryFn: async () => {
      try {
        return await HuginCollectionsService.findByTitleAndMaterialType(query);
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

  // Pick first as default once data arrives (don’t override user choice)
  useEffect(() => {
    if (!selected && q.data && q.data.length > 0) {
      //setSelected(q.data[0]);
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
