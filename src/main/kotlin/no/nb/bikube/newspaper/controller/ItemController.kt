package no.nb.bikube.newspaper.controller

import no.nb.bikube.core.model.Item
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/newspapers/items")
class ItemController (
    private val axiellService: AxiellService
){
    @GetMapping("/")
    fun getAllItems(): Flux<Item> = axiellService.getAllItems()
}
