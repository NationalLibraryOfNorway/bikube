package no.nb.bikube.catalogue.alma.controller

import jakarta.validation.ConstraintViolationException
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

}