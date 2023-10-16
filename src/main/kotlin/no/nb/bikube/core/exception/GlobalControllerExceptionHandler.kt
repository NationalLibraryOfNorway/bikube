package no.nb.bikube.core.exception

import no.nb.bikube.core.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.net.URI
import java.time.Instant

@ControllerAdvice
class GlobalControllerExceptionHandler {
    @ExceptionHandler(AxiellCollectionsException::class)
    fun handleAxiellCollectionsException(exception: AxiellCollectionsException): ProblemDetail {
        logger().error("AxiellCollectionsException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        problemDetail.detail = exception.message ?: "Error occurred when trying to contact Collections."
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(AxiellTitleNotFound::class)
    fun handleAxiellTitleNotFoundException(exception: AxiellTitleNotFound): ProblemDetail {
        logger().error("AxiellTitleNotFound occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Collections title not found: ${exception.message}"
        problemDetail.addDefaultProperties()

        return problemDetail
    }

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

    @ExceptionHandler(AxiellItemNotFound::class)
    fun handleAxiellItemNotFoundException(exception: AxiellItemNotFound): ProblemDetail {
        logger().warn("AxiellItemNotFound occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Collections item not found: ${exception.message}"
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(AxiellManifestationNotFound::class)
    fun handleAxiellManifestationNotFoundException(exception: AxiellManifestationNotFound): ProblemDetail {
        logger().warn("AxiellManifestationNotFound occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Collections manifestation not found: ${exception.message}"
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(AxiellYearWorkNotFound::class)
    fun handleAxiellYearWorkNotFoundException(exception: AxiellYearWorkNotFound): ProblemDetail {
        logger().warn("AxiellYearWorkNotFound occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Collections year work not found: ${exception.message}"
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
