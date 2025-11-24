package no.nb.bikube.catalogue.alma.enum

import no.nb.bikube.catalogue.alma.model.FieldID

enum class OtherField(override val tag: String, override val code: String) : FieldID {
    // 100 - Main Entry-Personal Name (NR)
    // $a - Personal name (NR)
    // https://www.loc.gov/marc/bibliographic/bd100.html
    AUTHOR("100", "a"),
    // 245 - Title Statement (NR)
    // $a - Title (NR)
    // https://www.loc.gov/marc/bibliographic/bd245.html
    TITLE("245", "a"),
    // 260 - Publication, Distribution, etc. (Imprint) (R)
    // $c - Date of publication, distribution, etc. (R)
    // https://www.loc.gov/marc/bibliographic/bd260.html
    YEAR("260", "c"),
    // 300 - Physical Description (R)
    // $a - Extent (R)
    // https://www.loc.gov/marc/bibliographic/bd300.html
    NUMBER_OF_PAGES("300", "a");
}
