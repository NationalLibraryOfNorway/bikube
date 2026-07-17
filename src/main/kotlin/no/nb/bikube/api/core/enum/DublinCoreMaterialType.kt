package no.nb.bikube.api.core.enum

import no.nb.bikube.api.core.exception.DublinCoreMissingFieldException
import kotlin.jvm.Throws

enum class DublinCoreMaterialType (val value: String) {
    NEWSPAPER("Avis"),
    MANUSCRIPT("Manuskript"),
    PERIODICAL("Tidsskrift"),
    BOOK("Bok");
}

@Throws (DublinCoreMissingFieldException::class)
fun materialTypeToDublinCoreMaterialType(materialType: MaterialType): DublinCoreMaterialType {
    return when (materialType) {
        MaterialType.NEWSPAPER -> DublinCoreMaterialType.NEWSPAPER
        MaterialType.MANUSCRIPT -> DublinCoreMaterialType.MANUSCRIPT
        MaterialType.PERIODICAL -> DublinCoreMaterialType.PERIODICAL
        MaterialType.MONOGRAPH -> DublinCoreMaterialType.BOOK
    }
}