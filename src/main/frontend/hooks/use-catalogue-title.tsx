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

  return {
    options: q.data ?? [],
    isLoading: q.isLoading,
    search: q.refetch,
  };
}
