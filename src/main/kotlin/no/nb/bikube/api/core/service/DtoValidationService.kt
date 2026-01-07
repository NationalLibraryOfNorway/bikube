package no.nb.bikube.api.core.service

import no.nb.bikube.api.catalogue.collections.enum.CollectionsItemStatus
import no.nb.bikube.api.core.exception.BadRequestBodyException
import no.nb.bikube.api.core.model.inputDto.ItemInputDto
import no.nb.bikube.api.core.model.inputDto.ItemUpdateDto
import org.springframework.stereotype.Service

@Service
class DtoValidationService {
    @Throws(BadRequestBodyException::class)
    fun validateItemInputDto(item: ItemInputDto) {
        val errorMessageList = mutableListOf<String>()

        if (item.titleCatalogueId.isBlank()) {
            errorMessageList.add("Need to provide title ID")
        }

        if (item.digital == null) {
            errorMessageList.add("Need to provide if item is digital or not (physical)")
        }

        if (item.digital == true) {
            if (item.urn.isNullOrBlank()) errorMessageList.add("Need to provide URN for digital item")
            if (item.number.isNullOrBlank()) errorMessageList.add("Need to provide number for digital item")
            if (item.volume.isNullOrBlank()) errorMessageList.add("Need to provide volume for digital item")
        }

        if (item.digital == false) {
            if (item.containerId.isNullOrBlank()) errorMessageList.add("Need to provide container ID for physical item")
            if (item.itemStatus != null) errorMessageList.add("Cannot provide item status for physical item")
        }

        if (item.itemStatus != null && CollectionsItemStatus.fromString(item.itemStatus) == null) {
            errorMessageList.add("Need to provide a valid item status. Expected: ${CollectionsItemStatus.entries.joinToString(" OR ") { it.value }}, received: ${item.itemStatus}")
        }

        if (errorMessageList.isNotEmpty()) {
            throw BadRequestBodyException(errorMessageList.joinToString("; "))
        }
    }

    @Throws(BadRequestBodyException::class)
    fun validateItemUpdateDto(dto: ItemUpdateDto) {
        if (dto.username.isBlank()) {
            throw BadRequestBodyException("Need to provide username")
        }

        if (dto.manifestationId.isBlank()) {
            throw BadRequestBodyException("Need to provide manifestation ID")
        }

        if (dto.notes == null && dto.number == null) {
            throw BadRequestBodyException("Need to provide either notes or number")
        }
    }
}
