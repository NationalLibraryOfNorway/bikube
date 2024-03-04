package no.nb.bikube.catalogue.alma.controller

import jakarta.validation.ConstraintViolationException
import no.nb.bikube.catalogue.alma.exception.AlmaException
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.stream.Collectors

@ControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler
    fun handlerConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(
            exception.constraintViolations
                .stream()
                .map { obj -> obj.message }
                .collect(Collectors.joining(", "))
        )
    }

    @ExceptionHandler
    fun handleAlmaRecordNotFoundException(exception: AlmaRecordNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(exception.message)
    }

    @ExceptionHandler
    fun handleAlmaException(exception: AlmaException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(exception.message)
    }
}
