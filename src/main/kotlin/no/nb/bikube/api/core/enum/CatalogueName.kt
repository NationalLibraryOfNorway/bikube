package no.nb.bikube.api.core.enum

import no.nb.bikube.api.core.exception.NotSupportedException

enum class CatalogueName(val value: String) {
    COLLECTIONS("COLLECTIONS"),
    HANSKE("HANSKE"),
    ALMA("ALMA")
}

@Throws(NotSupportedException::class)
fun materialTypeToCatalogueName(materialType: MaterialType): CatalogueName {
    return when (materialType) {
        MaterialType.NEWSPAPER -> CatalogueName.COLLECTIONS
        MaterialType.MANUSCRIPT -> CatalogueName.HANSKE
        MaterialType.PERIODICAL -> CatalogueName.ALMA
        MaterialType.MONOGRAPH -> CatalogueName.ALMA
    }
}
