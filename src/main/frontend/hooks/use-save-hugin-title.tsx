import {useMutation, useQueryClient} from '@tanstack/react-query';
import type HuginTitle from '@/generated/no/nb/bikube/hugin/model/HuginTitle';
import {keys} from "@/query/keys";
import {HuginNewspaperService} from "@/generated/endpoints";
import ContactInfo from "@/generated/no/nb/bikube/hugin/model/ContactInfo";
import ContactInfoDto from "@/generated/no/nb/bikube/hugin/model/dto/ContactInfoDto";
import ContactType from "@/generated/no/nb/bikube/hugin/model/ContactType";
import ContactUpdateDto from "@/generated/no/nb/bikube/hugin/model/dto/ContactUpdateDto";

type SavePayload = {
    id: number;
    vendor: string;
    contactName: string;
    shelf: string;
    notes: string;
    contactInfos: ContactInfo[];
};

const emptyToNull = (s?: string | null) => (s && s.trim().length ? s.trim() : undefined);

function normalize(p: SavePayload): ContactUpdateDto {
    return {
        id: p.id,
        vendor: emptyToNull(p.vendor),
        contactName: emptyToNull(p.contactName),
        shelf: emptyToNull(p.shelf),
        notes: emptyToNull(p.notes),
        contactInfos: p.contactInfos
            .filter(ci => !!ci.contactType && !!(ci.contactValue ?? "").trim())
            .map(ci => ({
                // cast is safe after the filter above
                contactType: ci.contactType as ContactType,
                contactValue: (ci.contactValue ?? "").trim(),
            })),
    };
}

export function useSaveHuginTitle() {
    const qc = useQueryClient();

    return useMutation({
        mutationKey: keys.saveHuginTitle(),
        mutationFn: async (payload: SavePayload) => {
            const body = normalize(payload);
            console.debug("upsertContactInformation payload:", body); // ADDED
            return await HuginNewspaperService.upsertContactInformation(body);
        },
        onSuccess: (saved) => {
            qc.setQueryData(keys.huginTitle(saved.id), saved);
        },
    });
}
