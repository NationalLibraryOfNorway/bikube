package no.nb.bikube.core.enum

enum class AxiellRecordType(val value: String) {
    WORK("WORK"),
    ITEM("ITEM"),
    MANIFESTATION("MANIFESTATION")
}

enum class AxiellDescriptionType(val value: String) {
    SERIAL("SERIAL"),
    YEAR("YEAR")
}

enum class AxiellFormat(val value: String) {
    DIGITAL("DIGITAL"),
    PHYSICAL("PHYSICAL")
}

enum class AxiellNameType(val value: String) {
    PUBLISHER("PUBLISHER")
}

enum class AxiellTermType(val value: String) {
    LANGUAGE("LANGUAGE")
}