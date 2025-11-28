package no.nb.bikube.newspaper.model

data class IdResponse(
    val priref: Int,
    val objectNumber: Int
)

fun IdResponse.toParsedIdResponse(): ParsedIdResponse {
    return ParsedIdResponse(
        priref = priref.toString(),
        objectNumber = "NP-" + objectNumber.toString().padStart(9, '0')
    )
}

data class ParsedIdResponse(
    val priref: String,
    val objectNumber: String
)