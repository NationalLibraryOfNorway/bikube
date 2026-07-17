package no.nb.bikube.api.core.model.dublinCore

import no.nb.bikube.api.core.enum.DublinCoreMaterialType

class DublinCoreMetadata (
    val type: DublinCoreMaterialType,
    val identifier: List<DublinCoreIdentifier>,
    val title: DublinCoreValue,
    val alternative: List<DublinCoreTypedValue>?,
    val creator: List<DublinCoreContributor>?,
    val contributor: List<DublinCoreContributor>?,
    val publisher: List<DublinCoreContributor>?,
    val spatial: List<DublinCoreSpatial>?,
    val date: List<DublinCoreTypedValue>?,
    val language: DublinCoreTypedValue?,
    val relation: List<DublinCoreRelation>?,
    val source: List<DublinCoreSource>?,
    val provenance: List<DublinCoreValue>?,
    val subject: List<DublinCoreValue>?,
    val description: List<DublinCoreValue>?
)

