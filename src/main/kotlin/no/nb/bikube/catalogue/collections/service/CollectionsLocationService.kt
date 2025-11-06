package no.nb.bikube.catalogue.collections.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.catalogue.collections.exception.CollectionsItemNotFound
import no.nb.bikube.catalogue.collections.model.CollectionsLocationObject
import no.nb.bikube.catalogue.collections.model.dto.CollectionsLocationDto
import no.nb.bikube.catalogue.collections.model.dto.createContainerDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CollectionsLocationService (
    private val collectionsService: CollectionsService
){
    fun createContainerIfNotExists(
        barcode: String,
        username: String
    ): Mono<CollectionsLocationObject> {
        return collectionsService.searchLocationAndContainers(barcode)
            .flatMap {
                if (it.hasObjects()) {
                    Mono.just(it.getFirstObject())
                } else {
                    createLocationRecord(barcode, username)
                }
            }
    }

    private fun createLocationRecord(
        barcode: String,
        username: String,
    ): Mono<CollectionsLocationObject> {
        val dto: CollectionsLocationDto = createContainerDto(barcode, username, null)
        val encodedBody = Json.encodeToString(dto)
        return collectionsService.createLocationRecord(encodedBody)
            .handle { collectionsLocationModel, sink ->
                if (collectionsLocationModel.hasObjects())
                    sink.next(collectionsLocationModel.getFirstObject())
                else
                    sink.error(CollectionsItemNotFound("New container not found"))
            }
    }
}
