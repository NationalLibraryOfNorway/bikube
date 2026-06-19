package no.nb.bikube.api.core.model.dublinCore

data class DublinCoreIdentifier(
    val type: String,
    val value: String
)

// lang should be ISO 639-3 (nob, nnn, eng)
// Technically optional, but should be set for all text items?

data class DublinCoreValue(
    val value: String,
    val lang: String
)

// For alternative: The use of the type attribute should be meaningful to the submitter,
// reflect the metadata catalog or system, and be applied consistently (using a standardized format).

// For date: The type of date and the corresponding year or value must be specified. ISO 8601-2 is the standard to be used.
// The use of the type attribute should be meaningful for the data provider, reflect the metadata catalog or system,
// and be applied consistently with a standardized format.

// For language: The type of language representation must be indicated.
// Examples of language types include subtitles, spoken language, written language, etc.
// Ulik dokumentasjon på submission service overview og metadata requirements. Sistnevnte uten lang, med språk i value
data class DublinCoreTypedValue(
    val type: String,
    val value: String,
    val lang: String?
)

data class DublinCoreAuthority(
    val source: String,
    val code: String,
    val uri: String
)

// The creator should also be identified using their full name (first name, last name/corporation).
// Birth and death years may be included in parentheses after the name. Examples: Nesbø, Jo (1960– ), Shakespeare, William (1564–1616).
data class DublinCoreContributor(
    val name: String,
    val type: String?, // person, korporasjon, konferanse, standardtittel
    val role: String?, // author, composer, film director, photographer, creator, etc.
    val authority: DublinCoreAuthority?
)

// Use ISO 3166-2 for specifying countries.
// Country codes should be placed in parentheses after the country name (e.g., Norway (NO)).
data class DublinCoreSpatial(
    val name: String,
    val type: String?,
    val authority: DublinCoreAuthority? = null,
    val coordinateReferenceSystem: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)