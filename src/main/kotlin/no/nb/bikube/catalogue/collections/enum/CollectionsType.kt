package no.nb.bikube.catalogue.collections.enum

enum class CollectionsRecordType(val value: String) {
    WORK("WORK"),
    ITEM("ITEM"),
    MANIFESTATION("MANIFESTATION");

    companion object {
        fun fromString(value: String?): CollectionsRecordType? {
            return entries.firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class CollectionsDescriptionType(val value: String) {
    SERIAL("SERIAL");

    companion object {
        fun fromString(value: String?): CollectionsDescriptionType? {
            return entries.firstOrNull { it.value.lowercase() == value?.lowercase() }
        }
    }
}

enum class CollectionsFormat(val value: String) {
    DIGITAL("DIGITAL"),
    PHYSICAL("PHYSICAL");

    companion object {
        fun fromString(value: String?): CollectionsFormat? {
            return entries.firstOrNull { it.value.lowercase() == value?.lowercase() }
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
