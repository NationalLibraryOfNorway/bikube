export const keys = {
    catalogueTitle: (id: string|number) => ['catalogueTitle', id] as const,
    catalogueTitles: (term: string|number) => ['catalogueTitleSearch', term] as const,
    huginTitle: (id: string|number) => ['huginTitle', id] as const,
    saveHuginTitle: () => ['saveHuginTitle'] as const,
};
