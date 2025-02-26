package no.nb.bikube.catalogue.collections.model

import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.util.DateUtils.Companion.parseYearOrDate
import java.time.LocalDate

// CollectionsModel
fun CollectionsModel.getFirstId(): String? {
    return this.getObjects()?.first()?.priRef
}

fun CollectionsModel.hasError(): Boolean {
    return this.getError() != null
}

fun CollectionsModel.getError(): String? {
    return this.adlibJson.diagnostic?.error?.message
}

// CollectionsObject
fun CollectionsObject.getUrn(): String? {
    return this.urn?.firstOrNull() ?: this.alternativeNumberList?.find { it.type == "URN" }?.value
}

fun CollectionsObject.getName(): String? {
    return this.titleList?.first()?.title
}

fun CollectionsObject.getDate(): LocalDate? {
    return this.date?.firstOrNull().let { parseYearOrDate(it) }
}

fun CollectionsObject.getStartDate(): LocalDate? {
    return this.datingList?.first()?.dateFrom?.let { parseYearOrDate(it) }
}

fun CollectionsObject.getEndDate(): LocalDate? {
    return this.datingList?.first()?.dateTo?.let { parseYearOrDate(it) }
}

fun CollectionsObject.getParentDate(): LocalDate? {
    return this.getFirstPartOf()?.getDate()
}

fun CollectionsObject.getMaterialType(): MaterialType? {
    return MaterialType.fromNorwegianString(this.subMediumList?.first()?.subMedium)
}

fun CollectionsObject.getMaterialTypeFromParent(): MaterialType? {
    // Migrated physical items are directly below title, digital items and new physical items are two levels below
    return this.getFirstPartOf()?.getMaterialType()
        ?: this.getFirstPartOf()?.getFirstPartOf()?.getMaterialType()
}

fun CollectionsObject.getTitleCatalogueId(): String? {
    val firstParent = this.getFirstPartOf()

    return if (firstParent?.getRecordType() == CollectionsRecordType.WORK) {
        firstParent.priRef
    } else {
        firstParent?.getFirstPartOf()?.priRef
    }
}

fun CollectionsObject.getTitleName(): String? {
    // Usually title is two levels above, but old physical items are one level below the item
    return this.getFirstPartOf()?.getFirstPartOf()?.getName() ?: this.getFirstPartOf()?.getName()
}

fun CollectionsObject.getFormat(): CollectionsFormat? {
    return CollectionsFormat.fromString(this.formatList?.first()?.first { it.lang == "neutral" }?.text)
}

fun CollectionsObject.getPublisher(): String? {
    return this.publisherList?.first()?.name
}

fun CollectionsObject.getPublisherPlace(): String? {
    return this.placeOfPublicationList?.first()?.name
}

fun CollectionsObject.getLanguage(): String? {
    return this.languageList?.first()?.language
}

fun CollectionsObject.getRecordType(): CollectionsRecordType? {
    return CollectionsRecordType.fromString(this.recordTypeList?.first()?.first{ it.lang == "neutral" }?.text)
}

fun CollectionsObject.getFirstPartOf(): CollectionsPartOfReference? {
    return this.partOfList?.first()?.partOfReference
}

fun CollectionsObject.getParts(): List<CollectionsPartsObject>? {
    return this.partsList
}

fun CollectionsObject.getParentId(): String? {
    return this.getFirstPartOf()?.priRef
}

// CollectionsPartsReference

fun CollectionsPartsReference.getName(): String? {
    return this.titleList?.first()?.title
}

fun CollectionsPartsReference.getDate(): LocalDate? {
    return this.date?.firstOrNull().let { parseYearOrDate(it) }
}

fun CollectionsPartsReference.getFormat(): CollectionsFormat? {
    return CollectionsFormat.fromString(this.formatList?.first()?.first { it.lang == "neutral" }?.text)
}

fun CollectionsPartsReference.getRecordType(): CollectionsRecordType? {
    return CollectionsRecordType.fromString(recordType?.first()?.first { langObj -> langObj.lang == "neutral" }?.text)
}

// CollectionsPartsObject
fun CollectionsPartsObject.getDate(): LocalDate? {
    return this.partsReference?.date?.firstOrNull().let { parseYearOrDate(it) }
}

// CollectionsPartOfReference
fun CollectionsPartOfReference.getName(): String? {
    return this.title?.first()?.title
}

fun CollectionsPartOfReference.getRecordType(): CollectionsRecordType? {
    return CollectionsRecordType.fromString(this.recordType?.first()?.first { langObj -> langObj.lang == "neutral" }?.text)
}

fun CollectionsPartOfReference.getMaterialType(): MaterialType? {
    return MaterialType.fromNorwegianString(this.subMedium?.first()?.subMedium)
}

fun CollectionsPartOfReference.getFirstPartOf(): CollectionsPartOfReference? {
    return this.partOfGroup?.first()?.partOfReference
}

fun CollectionsPartOfReference.getDate(): LocalDate? {
    return this.date?.firstOrNull().let { parseYearOrDate(it) }
}
