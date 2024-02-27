package no.nb.bikube.catalog.collections.enum

enum class CollectionsRecordType(val value: String) {
    WORK("WORK"),
    ITEM("ITEM"),
    MANIFESTATION("MANIFESTATION");

    companion object {
        fun fromString(value: String?): CollectionsRecordType? {
            return values().firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class CollectionsDescriptionType(val value: String) {
    SERIAL("SERIAL"),
    YEAR("YEAR");

    companion object {
        fun fromString(value: String?): CollectionsDescriptionType? {
            return values().firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class CollectionsFormat(val value: String) {
    DIGITAL("DIGITAL"),
    PHYSICAL("PHYSICAL");

    companion object {
        fun fromString(value: String?): CollectionsFormat? {
            return values().firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class CollectionsNameType(val value: String) {
    PUBLISHER("PUBL")
}

enum class CollectionsTermType(val value: String) {
    LANGUAGE("LANGUAGE"),
    LOCATION("PLACE")
}
