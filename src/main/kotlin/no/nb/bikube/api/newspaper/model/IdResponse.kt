package no.nb.bikube.api.newspaper.model

data class IdResponse(
    val priref: Int,
    val objectNumber: Int
)

fun IdResponse.toParsedIdResponse(): ParsedIdResponse {
    return ParsedIdResponse(
        priref = priref.toString(),
        objectNumber = objectNumber.toString().padStart(9, '0')
    )
}

data class ParsedIdResponse(
    val priref: String,
    val objectNumber: String
)
