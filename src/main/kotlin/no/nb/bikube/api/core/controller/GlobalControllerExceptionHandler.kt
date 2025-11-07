package no.nb.bikube.api.core.controller

import jakarta.validation.ConstraintViolationException
import no.nb.bikube.api.core.exception.BadRequestBodyException
import no.nb.bikube.api.core.exception.NotSupportedException
import no.nb.bikube.api.core.exception.RecordAlreadyExistsException
import no.nb.bikube.api.core.exception.SearchIndexNotAvailableException
import no.nb.bikube.api.core.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.net.URI
import java.time.Instant

@ControllerAdvice
class GlobalControllerExceptionHandler {

    @ExceptionHandler(NotSupportedException::class)
    fun handleNotSupportedException(exception: NotSupportedException): ProblemDetail {
        logger().warn("NotSupportedException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.detail = exception.message ?: "What you are trying to do is not supported."
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(BadRequestBodyException::class)
    fun handleBadRequestBodyException(exception: BadRequestBodyException): ProblemDetail {
        logger().warn("BadRequestBodyException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.detail = exception.message ?: "The request body is malformed."
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(RecordAlreadyExistsException::class)
    fun handleRecordAlreadyExistsException(exception: RecordAlreadyExistsException): ProblemDetail {
        logger().warn("RecordAlreadyExistsException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT)
        problemDetail.detail = exception.message ?: "The record already exists."
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler
    fun handlerConstraintViolationException(exception: ConstraintViolationException): ProblemDetail {
        logger().error("ConstraintViolationException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.detail = exception.message ?: "Validation error."
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler
    fun handlerSearchIndexNotAvailableException(exception: SearchIndexNotAvailableException): ProblemDetail {
        logger().error("SearchIndexNotAvailableException occurred")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE)
        problemDetail.detail = "The search index is unavailable"
        problemDetail.addDefaultProperties()

        return problemDetail
    }
}

fun ProblemDetail.addDefaultProperties() {
    this.type = URI(
        "https://produksjon.nb.no/bikube/error/" +
        "${HttpStatus.resolve(this.status)?.name?.lowercase()?.replace("_", "-")}"
    )
    this.setProperty("timestamp", Instant.now())
}
