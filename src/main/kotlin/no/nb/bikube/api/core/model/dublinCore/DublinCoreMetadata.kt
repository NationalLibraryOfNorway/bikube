package no.nb.bikube.api.core.model.dublinCore

import no.nb.bikube.api.core.enum.DublinCoreMaterialType

class DublinCoreMetadata (
    val type: DublinCoreMaterialType,
    val identifier: List<DublinCoreIdentifier>,
    val title: DublinCoreValue,
    val alternative: List<DublinCoreTypedValue>?, // SHOULD
    val creator: List<DublinCoreContributor>?, // SHOULD
    val contributor: List<DublinCoreContributor>?, // SHOULD
    val publisher: List<DublinCoreContributor>?, // SHOULD
    val spatial: List<DublinCoreSpatial>?, // SHOULD
    val date: DublinCoreTypedValue?, // SHOULD
    val language: DublinCoreTypedValue?, // SHOULD
    val relation: List<DublinCoreRelation>?, // SHOULD
    val source: List<DublinCoreSource>?, // SHOULD
    val provenance: List<DublinCoreValue>?, // SHOULD
    val subject: List<DublinCoreValue>?, // MAY
    val description: List<DublinCoreValue>? // MAY

)

