export const keys = {
    catalogueTitle: (id: any) => ['catalogueTitle', id] as const,
    catalogueTitles: (term: any) => ['catalogueTitleSearch', term] as const,
    huginTitle: (id: any) => ['huginTitle', id] as const,
};
