export const keys = {
    catalogueTitle: (id: string|number|undefined) => ['catalogueTitle', id] as const,
    catalogueTitles: (term: string|number|undefined) => ['catalogueTitleSearch', term] as const,
    huginTitle: (id: string|number|undefined) => ['huginTitle', id] as const,
    saveHuginTitle: () => ['saveHuginTitle'] as const,
};
