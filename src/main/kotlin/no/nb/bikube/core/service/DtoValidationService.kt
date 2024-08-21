package no.nb.bikube.core.service

import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.model.inputDto.ItemInputDto
import no.nb.bikube.core.model.inputDto.ItemUpdateDto
import org.springframework.stereotype.Service

@Service
class DtoValidationService {
    @Throws(BadRequestBodyException::class)
    fun validateItemInputDto(item: ItemInputDto) {
        if (item.titleCatalogueId.isBlank()) {
            throw BadRequestBodyException("Need to provide title ID")
        }

        if (item.digital == null) {
            throw BadRequestBodyException("Need to provide if item is digital or not (physical)")
        }

        if (item.digital == true && item.urn.isNullOrBlank()) {
            throw BadRequestBodyException("Need to provide URN for digital item")
        }
    }

    @Throws(BadRequestBodyException::class)
    fun validateItemUpdateDto(dto: ItemUpdateDto) {
        if (dto.date == null && dto.notes == null && dto.number == null) {
            throw BadRequestBodyException("Need to provide at least one field to update")
        }

        if (dto.username.isBlank()) {
            throw BadRequestBodyException("Need to provide username")
        }

        if (dto.manifestationId.isBlank()) {
            throw BadRequestBodyException("Need to provide manifestation ID")
        }
    }
}