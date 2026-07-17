package no.nb.bikube.api.core.model.dublinCore

data class DublinCoreIdentifier(
    val type: String,
    val value: String
)

data class DublinCoreValue(
    val value: String,
    val lang: String
)

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

data class DublinCoreContributor(
    val name: String,
    val type: String?,
    val role: String?,
    val authority: DublinCoreAuthority?
)

data class DublinCoreSpatial(
    val name: String,
    val type: String?,
    val authority: DublinCoreAuthority? = null,
    val coordinateReferenceSystem: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class DublinCoreRelation(
    val id: String,
    val type: String,
    val title: String? = null,
    val lang: String? = null,
    val uri: String? = null,
)

data class DublinCoreSource(
    val identifier: DublinCoreIdentifier,
    val description: String? = null,
    val uri: String? = null,
)