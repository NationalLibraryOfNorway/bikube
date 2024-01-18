package no.nb.bikube.core.enum

enum class MaterialType (val value: String, val norwegian: String) {
    NEWSPAPER("Newspaper", "Aviser"),
    MANUSCRIPT("Manuscript", "Manuskript"),
    PERIODICAL("Periodical", "Tidsskrift"),
    MONOGRAPH("Monograph", "Monografi");

    companion object {
        fun fromNorwegianString(value: String?): MaterialType? {
            return values().firstOrNull { it.norwegian.lowercase() == value?.lowercase() }
        }
    }
}
