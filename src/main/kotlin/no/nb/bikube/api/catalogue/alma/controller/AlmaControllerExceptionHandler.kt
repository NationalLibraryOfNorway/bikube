package no.nb.bikube.catalogue.alma.controller

import no.nb.bikube.catalogue.alma.exception.AlmaException
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.core.controller.addDefaultProperties
import no.nb.bikube.core.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class AlmaControllerExceptionHandler {

    @ExceptionHandler
    fun handleAlmaRecordNotFoundException(exception: AlmaRecordNotFoundException): ProblemDetail {
        logger().error("AlmaRecordNotFoundException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Alma record not found: ${exception.message}"
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler
    fun handleAlmaException(exception: AlmaException): ProblemDetail {
        logger().error("AlmaException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        problemDetail.detail = exception.message ?: "Error occurred when trying to contact Alma."
        problemDetail.addDefaultProperties()

        return problemDetail
    }
}
