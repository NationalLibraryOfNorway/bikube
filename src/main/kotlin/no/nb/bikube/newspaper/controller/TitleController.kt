package no.nb.bikube.newspaper.controller

import no.nb.bikube.core.model.Title
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/newspapers")
class TitleController (
    private val axiellService: AxiellService
) {
    @GetMapping("/")
    fun getTitles(): Flux<Title> {
        return axiellService.getTitles()
    }
}