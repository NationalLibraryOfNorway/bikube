package no.nb.bikube.core.exception

import no.nb.bikube.core.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalControllerExceptionHandler {
    @ExceptionHandler(AxiellCollectionsException::class)
    fun handleAxiellCollectionsException(exception: AxiellCollectionsException): ProblemDetail {
        logger().error("AxiellCollectionsException occurred: ${exception.message}")
        return ProblemDetail
            .forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.message ?: "Error occurred when trying to contact Collection."
            )
    }

    @ExceptionHandler(AxiellTitleNotFound::class)
    fun handleAxiellTitleNotFoundException(exception: AxiellTitleNotFound): ProblemDetail {
        logger().error("AxiellTitleNotFound occurred: ${exception.message}")
        return ProblemDetail
            .forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Collections title not found: ${exception.message}"
            )
    }
}
