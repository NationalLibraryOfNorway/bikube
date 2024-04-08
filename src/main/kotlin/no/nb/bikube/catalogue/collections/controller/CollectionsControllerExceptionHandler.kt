package no.nb.bikube.catalogue.collections.controller

import no.nb.bikube.catalogue.collections.exception.CollectionsException
import no.nb.bikube.catalogue.collections.exception.CollectionsItemNotFound
import no.nb.bikube.catalogue.collections.exception.CollectionsManifestationNotFound
import no.nb.bikube.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.core.controller.addDefaultProperties
import no.nb.bikube.core.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CollectionsControllerExceptionHandler {

    @ExceptionHandler(CollectionsException::class)
    fun handleCollectionsException(exception: CollectionsException): ProblemDetail {
        logger().error("CollectionsException occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        problemDetail.detail = exception.message ?: "Error occurred when trying to contact Collections."
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(CollectionsTitleNotFound::class)
    fun handleCollectionsTitleNotFoundException(exception: CollectionsTitleNotFound): ProblemDetail {
        logger().warn("CollectionsTitleNotFound occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Collections title not found: ${exception.message}"
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(CollectionsItemNotFound::class)
    fun handleCollectionsItemNotFoundException(exception: CollectionsItemNotFound): ProblemDetail {
        logger().warn("CollectionsItemNotFound occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Collections item not found: ${exception.message}"
        problemDetail.addDefaultProperties()

        return problemDetail
    }

    @ExceptionHandler(CollectionsManifestationNotFound::class)
    fun handleCollectionsManifestationNotFoundException(exception: CollectionsManifestationNotFound): ProblemDetail {
        logger().warn("CollectionsManifestationNotFound occurred: ${exception.message}")

        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.detail = "Collections manifestation not found: ${exception.message}"
        problemDetail.addDefaultProperties()

        return problemDetail
    }
}
