package no.nb.bikube.catalogue.alma.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.MultiValueMap

@ConfigurationProperties("alma")
class AlmaConfig(
    val almaSruUrl: String,
    val almaWsUrl: String,
    val apiKey: String,
    val commonParams: MultiValueMap<String, String> = MultiValueMap.fromSingleValue(
        mapOf(
            "version" to "1.2",
            "operation" to "searchRetrieve",
            "recordSchema" to "marcxml"
        )
    )
)
