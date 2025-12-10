package no.nb.bikube.api.core.controller

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Hidden
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

