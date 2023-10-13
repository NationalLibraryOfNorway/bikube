package no.nb.bikube.core.enum

enum class AxiellRecordType(val value: String) {
    WORK("WORK"),
    ITEM("ITEM"),
    MANIFESTATION("MANIFESTATION");

    companion object {
        fun fromString(value: String?): AxiellRecordType? {
            return values().firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class AxiellDescriptionType(val value: String) {
    SERIAL("SERIAL"),
    YEAR("YEAR");

    companion object {
        fun fromString(value: String?): AxiellDescriptionType? {
            return values().firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class AxiellFormat(val value: String) {
    DIGITAL("DIGITAL"),
    PHYSICAL("PHYSICAL");

    companion object {
        fun fromString(value: String?): AxiellFormat? {
            return values().firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class AxiellNameType(val value: String) {
    PUBLISHER("PUBL")
}

enum class AxiellTermType(val value: String) {
    LANGUAGE("LANGUAGE"),
    LOCATION("PLACE")
}
