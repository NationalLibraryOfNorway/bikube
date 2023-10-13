package no.nb.bikube.core.model.collections

import no.nb.bikube.core.enum.AxiellDescriptionType

fun CollectionsObject.isSerial(): Boolean {
    return this.workTypeList?.first()?.first()?.text == AxiellDescriptionType.SERIAL.value
}

fun CollectionsObject.getUrn(): String? {
    return this.alternativeNumberList?.find { it.type == "URN" }?.value
}
