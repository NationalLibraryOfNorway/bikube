package no.nb.bikube.api.core.model.dublinCore

import no.nb.bikube.api.core.enum.DublinCoreMaterialType

class DublinCoreMetadata (
    val type: DublinCoreMaterialType,
    val identifier: List<DublinCoreIdentifier>,
    val title: DublinCoreValue,
    val alternative: List<DublinCoreTypedValue>?, // SHOULD
    val creator: List<DublinCoreContributor>?, // SHOULD
    val contributor: List<DublinCoreContributor>?, // SHOULD e.g illustrator, photographer, co-author
    val publisher: List<DublinCoreContributor>?, // SHOULD
    val spatial: List<DublinCoreSpatial>?, // SHOULD
    val date: DublinCoreTypedValue?, // SHOULD
    val language: DublinCoreTypedValue?, // SHOULD
    val relation: List<DublinCoreValue>?, // SHOULD
    val provenance: List<DublinCoreValue>?, // SHOULD
    val subject: List<DublinCoreValue>?, // MAY
    val description: List<DublinCoreValue>? // MAY

)

