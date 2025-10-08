package no.nb.bikube.core.enum

enum class MaterialType (val value: String, val norwegian: String) {
    NEWSPAPER("Newspaper", "Avis"),
    MANUSCRIPT("Manuscript", "Manuskript"),
    PERIODICAL("Periodical", "Tidsskrift"),
    MONOGRAPH("Monograph", "Monografi");

    companion object {
        fun fromNorwegianString(value: String?): MaterialType? {
            return entries.firstOrNull { it.norwegian.lowercase() == value?.lowercase() } ?: if(value == "Aviser") return NEWSPAPER else null
        }
    }
}
