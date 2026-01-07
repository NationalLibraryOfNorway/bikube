package no.nb.bikube.api.catalogue.collections.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("collections.lref")
class CollectionsLrefConfig (
    val mavisId: String,
    val argang: String,
    val avisnr: String,
    val versjon: String,
    val originalTittel: String,
    val alternativTittel: String,
    val mediumTekst: String,
    val submediumAviser: String,
    val itemStatusPliktavlevert: String,
)
