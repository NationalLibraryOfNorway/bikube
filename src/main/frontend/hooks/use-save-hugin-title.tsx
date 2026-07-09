import {useQueryClient} from '@tanstack/react-query';
import {keys} from "@/query/keys";
import {
    useUpsertContactInformation,
    type ContactUpdateDto,
    type ContactInfo,
    type ContactInfoDtoContactType,
} from '@/src/api/bikubeAPIForKommuniksjonMedTekstkataloger';

type SavePayload = {
    id: number;
    vendor?: string;
    contactName?: string;
    shelf?: string;
    notes?: string;
    contactInfos?: ContactInfo[];
    releasePattern?: number[];
};

const emptyToUndef = (s?: string | null) => (s && s.trim().length ? s.trim() : undefined);

function normalize(p: SavePayload): ContactUpdateDto {
    return {
        id: p.id,
        vendor: emptyToUndef(p.vendor),
        contactName: emptyToUndef(p.contactName),
        shelf: emptyToUndef(p.shelf),
        notes: emptyToUndef(p.notes),
        contactInfos:
            p.contactInfos?.filter(ci => !!ci.contactType && !!(ci.contactValue ?? "").trim())
            .map(ci => ({
                contactType: ci.contactType! as ContactInfoDtoContactType,
                contactValue: (ci.contactValue ?? "").trim(),
            })),
        releasePattern: p.releasePattern?.map((v) => Math.trunc(Number(v))),
    };
}

export function useSaveHuginTitle() {
    const queryClient = useQueryClient();
    const mutation = useUpsertContactInformation();

    return {
        ...mutation,
        mutateAsync: async (payload: SavePayload) => {
            const body = normalize(payload);
            const saved = await mutation.mutateAsync({ data: body });
            if (saved) queryClient.setQueryData(keys.huginTitle(saved.id!), saved);
            return saved;
        },
    };
}
