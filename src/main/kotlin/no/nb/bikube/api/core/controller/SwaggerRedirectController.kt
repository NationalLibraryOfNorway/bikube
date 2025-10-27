package no.nb.bikube.api.core.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
class SwaggerRedirectController {

    @GetMapping("/")
    fun root(): String =
        "redirect:/swagger-ui/index.html"
}
