package no.nb.bikube.api.catalogue.collections.enum

enum class CollectionsDatabase (val value: String) {
    NEWSPAPER("newspaper"), //TODO: Endre til newspaper når det er på plass i stage
    PEOPLE("people"),
    LANGUAGES("thesau"),
    GEO_LOCATIONS("thesaugeo"),
    LOCATIONS("location"),
}
