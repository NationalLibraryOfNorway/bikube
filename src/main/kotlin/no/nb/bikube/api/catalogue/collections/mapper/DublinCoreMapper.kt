package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.model.CollectionsObject
import no.nb.bikube.api.catalogue.collections.model.CollectionsPartOfReference
import no.nb.bikube.api.catalogue.collections.model.getAlternativeNames
import no.nb.bikube.api.catalogue.collections.model.getDate
import no.nb.bikube.api.catalogue.collections.model.getInputNotes
import no.nb.bikube.api.catalogue.collections.model.getItemStatus
import no.nb.bikube.api.catalogue.collections.model.getMaterialTypeFromParent
import no.nb.bikube.api.catalogue.collections.model.getName
import no.nb.bikube.api.catalogue.collections.model.getPublisher
import no.nb.bikube.api.catalogue.collections.model.getPublisherPlace
import no.nb.bikube.api.catalogue.collections.model.getUrn
import no.nb.bikube.api.core.enum.DublinCoreMaterialType
import no.nb.bikube.api.core.enum.materialTypeToDublinCoreMaterialType
import no.nb.bikube.api.core.exception.DublinCoreMissingFieldException
import no.nb.bikube.api.core.model.dublinCore.DublinCoreContributor
import no.nb.bikube.api.core.model.dublinCore.DublinCoreIdentifier
import no.nb.bikube.api.core.model.dublinCore.DublinCoreMetadata
import no.nb.bikube.api.core.model.dublinCore.DublinCoreRelation
import no.nb.bikube.api.core.model.dublinCore.DublinCoreSpatial
import no.nb.bikube.api.core.model.dublinCore.DublinCoreTypedValue
import no.nb.bikube.api.core.model.dublinCore.DublinCoreValue

fun mapCollectionsObjectToDublinCoreMetadata(
    itemModel: CollectionsObject,
    manifestationId: String,
    manifestationData: CollectionsPartOfReference,
    titleId: String,
    titleData: CollectionsPartOfReference,
    lang: String
): DublinCoreMetadata {
    return DublinCoreMetadata(
        identifier = getIdentifiers(itemModel),
        type = getMaterialType(itemModel),
        title = getTitle(titleData, lang),
        alternative = getAlternativeTitles(titleData, lang).ifEmpty { null },
        creator = null,
        contributor = null,
        publisher = getPublisher(titleData),
        spatial = getSpatial(titleData),
        date = if (itemModel.getDate() != null) { listOf(
            DublinCoreTypedValue(
                type = "Published",
                value = itemModel.getDate()!!.toString(),
                lang = null
            )
        ) } else null,
        language = DublinCoreTypedValue(
            type = "written language",
            value = lang,
            lang = null
        ),
        relation = listOf(
            DublinCoreRelation(
                id = manifestationId,
                type = "isPartOf",
                title = manifestationData.getName(),
                lang = lang
            ),
            DublinCoreRelation(
                id = titleId,
                type = "isPartOf",
                title = titleData.getName(),
                lang = lang
            )
        ),
        source = null,
        provenance = null,
        subject = null,
        description = getDescriptions(itemModel).ifEmpty { null },
    )
}

fun getIdentifiers(itemModel: CollectionsObject): List<DublinCoreIdentifier> {
    val identifiers = mutableListOf(
        DublinCoreIdentifier(
            type = "CatalogueId",
            value = itemModel.priRef
        )
    )

    if (itemModel.getUrn() != null) {
        identifiers.add(
            DublinCoreIdentifier(
                type = "URN",
                value = itemModel.getUrn()!!
            )
        )
    }

    return identifiers
}

fun getMaterialType(itemModel: CollectionsObject): DublinCoreMaterialType {
    val materialType = itemModel.getMaterialTypeFromParent()
        ?: throw DublinCoreMissingFieldException("Missing materialType for item object with id ${itemModel.priRef}")
    return materialTypeToDublinCoreMaterialType(materialType)
}

fun getTitle(titleData: CollectionsPartOfReference, lang: String): DublinCoreValue {
    val title = titleData.getName()
        ?: throw DublinCoreMissingFieldException("Missing title for item object with id ${titleData.priRef}")
    return DublinCoreValue(
        value = title,
        lang = lang
    )
}

fun getAlternativeTitles(titleData: CollectionsPartOfReference, lang: String): List<DublinCoreTypedValue> {
    val alternativeTitles = mutableListOf<DublinCoreTypedValue>()
    titleData.getAlternativeNames().forEach {
        if (it.title != null && it.titleType != null) {
            alternativeTitles.add(
                DublinCoreTypedValue(
                    value = it.title,
                    type = it.titleType,
                    lang = lang
                )
            )
        }
    }
    return alternativeTitles.toList()
}

fun getPublisher(titleData: CollectionsPartOfReference): List<DublinCoreContributor>? {
    val publisher = titleData.getPublisher() ?: titleData.getName() ?: return null

    return listOf(
        DublinCoreContributor(
            name = publisher,
            type = "korporasjon",
            role = null,
            authority = null
        )
    )
}

fun getSpatial(titleData: CollectionsPartOfReference): List<DublinCoreSpatial>? {
    val place = titleData.getPublisherPlace() ?: return null
    return listOf(
        DublinCoreSpatial(
            name = interpolateCountryCode(place),
            type = "Place of publication",
        )
    )
}

fun interpolateCountryCode(publisherPlace: String): String {
    val firstSeparator = publisherPlace.indexOf(";")

    if (firstSeparator == -1) {
        throw Error("Unexpected format of country when interpolating country code: $publisherPlace")
    }

    val country = publisherPlace.substring(0, firstSeparator)
    val rest = publisherPlace.substring(firstSeparator)

    return when (country.lowercase()) {
        "norge" -> "$country (NO)$rest"
        "usa" -> "$country (US)$rest"
        "canada" -> "$country (CA)$rest"
        "storbritannia" -> "$country (GB)$rest"
        else -> throw Error("Unexpected country when interpolating country code: $country")
    }
}

fun getDescriptions(itemModel: CollectionsObject): List<DublinCoreValue> {
    val descriptions = mutableListOf<DublinCoreValue>()
    if (itemModel.getItemStatus() != null) {
        descriptions.add(
            DublinCoreValue(
                value = itemModel.getItemStatus()!!,
                lang = "nob"
            )
        )
    }
    if (itemModel.getInputNotes() != null) {
        itemModel.getInputNotes()!!.forEach {
            descriptions.add(
                DublinCoreValue(
                    value = it,
                    lang = "nob"
                )
            )
        }
    }
    return descriptions
}