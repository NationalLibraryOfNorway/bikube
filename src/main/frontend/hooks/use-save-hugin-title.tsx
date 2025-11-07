import {useMutation, useQueryClient} from '@tanstack/react-query';
import {keys} from "@/query/keys";
import {HuginNewspaperService} from "@/generated/endpoints";
import ContactInfo from "@/generated/no/nb/bikube/hugin/model/ContactInfo";
import ContactType from "@/generated/no/nb/bikube/hugin/model/ContactType";
import ContactUpdateDto from "@/generated/no/nb/bikube/hugin/model/dto/ContactUpdateDto";

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
                contactType: ci.contactType! as ContactType,
                contactValue: (ci.contactValue ?? "").trim(),
            })),
        releasePattern: p.releasePattern?.map((v) => Math.trunc(Number(v))),
    };
}

export function useSaveHuginTitle() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationKey: keys.saveHuginTitle(),
        mutationFn: async (payload: SavePayload) => {
            const body = normalize(payload);
            return await HuginNewspaperService.upsertContactInformation(body);
        },
        onSuccess: (saved) => {
            queryClient.setQueryData(keys.huginTitle(saved.id), saved);
        },
    });
}
