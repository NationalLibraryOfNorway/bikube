package no.nb.bikube.newspaper.controller

import no.nb.bikube.core.model.Item
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/newspapers/items")
class ItemController (
    private val axiellService: AxiellService
){
    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllItems(): ResponseEntity<Flux<Item>> {
        return ResponseEntity.ok(axiellService.getAllItems())
    }
}
