package no.nb.bikube.api.core.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class SwaggerRedirectController {

    @GetMapping("/")
    fun root(): ResponseEntity<Void> {
        return ResponseEntity
            .status(HttpStatus.MOVED_PERMANENTLY)
            .location(URI.create("/bikube/swagger-ui/index.html"))
            .build()
    }
}

