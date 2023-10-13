package no.nb.bikube.core.model.collections

import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.util.DateUtils.Companion.parseYearOrDate
import java.time.LocalDate

// CollectionsModel
fun CollectionsModel.getObjects(): List<CollectionsObject>? {
    return this.adlibJson.recordList
}

fun CollectionsModel.getFirstObject(): CollectionsObject? {
    return this.getObjects()?.first()
}

// CollectionsObject
fun CollectionsObject.isSerial(): Boolean {
    return this.workTypeList?.first()?.first()?.text == AxiellDescriptionType.SERIAL.value
}

fun CollectionsObject.getUrn(): String? {
    return this.alternativeNumberList?.find { it.type == "URN" }?.value
}

fun CollectionsObject.getName(): String? {
    return this.titleList?.first()?.title
}

fun CollectionsObject.getItemDate(): LocalDate? {
    return this.titleList?.first()?.title.let { parseYearOrDate(it?.takeLast(10)) }
}

fun CollectionsObject.getStartDate(): LocalDate? {
    return this.datingList?.first()?.dateFrom?.let { parseYearOrDate(it) }
}

fun CollectionsObject.getEndDate(): LocalDate? {
    return this.datingList?.first()?.dateTo?.let { parseYearOrDate(it) }
}

fun CollectionsObject.getMaterialType(): MaterialType? {
    return MaterialType.fromNorwegianString(this.subMediumList?.first()?.subMedium)
}

fun CollectionsObject.getMaterialTypeFromParent(): MaterialType? {
    return MaterialType.fromNorwegianString(
        this.partOfList?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.subMedium?.first()?.subMedium
    )
}

fun CollectionsObject.getTitleCatalogueId(): String? {
    return this.partOfList?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.priRef
}

fun CollectionsObject.getTitleName(): String? {
    return this.partOfList?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.title?.first()?.title
}

fun CollectionsObject.getFormat(): AxiellFormat? {
    return AxiellFormat.fromString(this.formatList?.first()?.first { it.lang == "neutral" }?.text)
}

fun CollectionsObject.getPublisher(): String? {
    return this.publisherList?.first()
}

fun CollectionsObject.getPublisherPlace(): String? {
    return this.placeOfPublicationList?.first()
}

fun CollectionsObject.getLanguage(): String? {
    return this.languageList?.first()?.language
}

fun CollectionsObject.getRecordType(): AxiellRecordType? {
    return AxiellRecordType.fromString(this.recordTypeList?.first()?.first{ it.lang == "neutral" }?.text)
}

fun CollectionsObject.getWorkType(): AxiellDescriptionType? {
    return AxiellDescriptionType.fromString(this.workTypeList?.first()?.first{ it.lang == "neutral" }?.text)
}

fun CollectionsObject.getFirstPartOf(): CollectionsPartOfReference? {
    return this.partOfList?.first()?.partOfReference
}

// CollectionsPartsReference

fun CollectionsPartsReference.getName(): String? {
    return this.titleList?.first()?.title
}

fun CollectionsPartsReference.getItemDate(): LocalDate? {
    return this.titleList?.first()?.title.let { parseYearOrDate(it?.takeLast(10)) }
}

fun CollectionsPartsReference.getFormat(): AxiellFormat? {
    return AxiellFormat.fromString(this.formatList?.first()?.first { it.lang == "neutral" }?.text)
}

fun CollectionsPartsReference.getRecordType(): AxiellRecordType? {
    return AxiellRecordType.fromString(recordType?.first()?.first { langObj -> langObj.lang == "neutral" }?.text)
}

fun CollectionsPartsReference.getWorkType(): AxiellDescriptionType? {
    return AxiellDescriptionType.fromString(workTypeList?.first()?.first { langObj -> langObj.lang == "neutral" }?.text)
}

// CollectionsPartsObject
fun CollectionsPartsObject.getPartRefs(): List<CollectionsPartsObject> {
    return this.partsReference?.partsList ?: emptyList()
}

// CollectionsPartOfReference
fun CollectionsPartOfReference.getName(): String? {
    return this.title?.first()?.title
}

fun CollectionsPartOfReference.getRecordType(): AxiellRecordType? {
    return AxiellRecordType.fromString(this.recordType?.first()?.first { langObj -> langObj.lang == "neutral" }?.text)
}

fun CollectionsPartOfReference.getMaterialType(): MaterialType? {
    return MaterialType.fromNorwegianString(this.subMedium?.first()?.subMedium)
}

fun CollectionsPartOfReference.getFirstPartOf(): CollectionsPartOfReference? {
    return this.partOfGroup?.first()?.partOfReference
}

fun CollectionsPartOfReference.getWorkType(): AxiellDescriptionType? {
    return AxiellDescriptionType.fromString(this.workTypeList?.first()?.first{ it.lang == "neutral" }?.text)
}

// CollectionsNameModel
fun CollectionsNameModel.getObjects(): List<CollectionsNameObject>? {
    return this.adlibJson.recordList
}

fun CollectionsNameModel.getFirstObject(): CollectionsNameObject? {
    return this.getObjects()?.first()
}

//CollectionsTermModel
fun CollectionsTermModel.getObjects(): List<CollectionsTermObject>? {
    return this.adlibJson.recordList
}

fun CollectionsTermModel.getFirstObject(): CollectionsTermObject? {
    return this.getObjects()?.first()
}
