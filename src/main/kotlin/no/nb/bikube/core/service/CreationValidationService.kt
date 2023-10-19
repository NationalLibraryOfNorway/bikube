package no.nb.bikube.core.service

import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.model.ItemInputDto
import org.springframework.stereotype.Service

@Service
class CreationValidationService {
    @Throws(BadRequestBodyException::class)
    fun validateItem(item: ItemInputDto) {
        if (item.titleCatalogueId.isNullOrBlank()) {
            throw BadRequestBodyException("Need to provide title ID")
        }

        if (item.date == null) {
            throw BadRequestBodyException("Need to provide date")
        }

        if (item.digital == null) {
            throw BadRequestBodyException("Need to provide if item is digital or not (physical)")
        }

        if (item.digital == true && item.urn.isNullOrBlank()) {
            throw BadRequestBodyException("Need to provide URN for digital item")
        }
    }
}
