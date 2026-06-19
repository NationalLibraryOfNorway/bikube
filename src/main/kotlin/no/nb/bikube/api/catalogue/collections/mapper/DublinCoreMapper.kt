package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.model.CollectionsObject
import no.nb.bikube.api.catalogue.collections.model.getDate
import no.nb.bikube.api.catalogue.collections.model.getLanguage
import no.nb.bikube.api.catalogue.collections.model.getMaterialTypeFromParent
import no.nb.bikube.api.catalogue.collections.model.getName
import no.nb.bikube.api.catalogue.collections.model.getPublisherPlace
import no.nb.bikube.api.catalogue.collections.model.getUrn
import no.nb.bikube.api.core.enum.materialTypeToDublinCoreMaterialType
import no.nb.bikube.api.core.exception.DublinCoreMissingFieldException
import no.nb.bikube.api.core.model.dublinCore.DublinCoreIdentifier
import no.nb.bikube.api.core.model.dublinCore.DublinCoreMetadata
import no.nb.bikube.api.core.model.dublinCore.DublinCoreSpatial
import no.nb.bikube.api.core.model.dublinCore.DublinCoreTypedValue
import no.nb.bikube.api.core.model.dublinCore.DublinCoreValue

fun mapCollectionsObjectToDublinCoreMetadata(
    itemModel: CollectionsObject,
    manifestationModel: CollectionsObject,
    titleModel: CollectionsObject
): DublinCoreMetadata {
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

    val type = if (itemModel.getMaterialTypeFromParent() != null) {
        materialTypeToDublinCoreMaterialType(itemModel.getMaterialTypeFromParent()!!)
    } else {
        throw DublinCoreMissingFieldException("Missing materialType for object with id ${itemModel.priRef}")
    }

    val title = itemModel.getName() ?: throw DublinCoreMissingFieldException("Missing title for object with id ${itemModel.priRef}")

    return DublinCoreMetadata(
        type = type,
        identifier = identifiers,
        title = DublinCoreValue(
            value = title,
            lang = "nob" // Infer from language?
        ),
        alternative = null, // Not relevant?
        creator = null, // Not relevant?
        contributor = null, // Not relevant?
        publisher = null, // Should exist?
        spatial = if (titleModel.getPublisherPlace() != null) { listOf(
            DublinCoreSpatial(
                name = interpolateCountryCode(titleModel.getPublisherPlace()!!),
                type = "Place of publication",
            )
        ) } else null,
        date = if (itemModel.getDate() != null) { DublinCoreTypedValue(
            type = "Published",
            value = itemModel.getDate()!!.toString(),
            lang = null
        ) } else null,
        language = if (titleModel.getLanguage() != null) { DublinCoreTypedValue(
            type = "written language",
            value = mapCollectionsLanguageToDublinCoreLanguage(titleModel.getLanguage()!!),
            lang = null
        ) } else null,
        relation = null,
        provenance = null,
        subject = null,
        description = null
    )
}

// TODO: Add all relevant languages
fun mapCollectionsLanguageToDublinCoreLanguage(inputLanguage: String): String {
    return when (inputLanguage.lowercase()) {
        "norsk bokmål" -> "nob"
//        "norsk nynorsk" -> "nnn"
//        "english" -> "eng"
        else -> throw Error("Unexpected language: $inputLanguage")
    }
}

// TODO: Add all relevant countries
fun interpolateCountryCode(publisherPlace: String): String {
    val firstSeparator = publisherPlace.indexOf(";")
    val country = publisherPlace.substring(0, firstSeparator)
    val rest = publisherPlace.substring(firstSeparator)

    return when (country.lowercase()) {
        "norge" -> "$country (NO)$rest"
        else -> throw Error("Unexpected country: $country")
    }
}