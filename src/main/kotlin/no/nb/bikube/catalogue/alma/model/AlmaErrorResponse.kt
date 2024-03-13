package no.nb.bikube.catalogue.alma.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "web_service_result")
data class AlmaErrorResponse (
    val errorExist: Boolean,
    @JacksonXmlElementWrapper val errorList: List<AlmaError>
)

@JacksonXmlRootElement(localName = "error")
data class AlmaError (
    val errorCode: String,
    val errorMessage: String
)

enum class AlmaErrorCode(val value: String) {
    NOT_FOUND("402203"),
    ILLEGAL_ARG("402204"),
    SERVER_ERROR("401652")
}
