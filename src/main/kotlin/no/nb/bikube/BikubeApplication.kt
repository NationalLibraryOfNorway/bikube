package no.nb.bikube

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BikubeApplication

fun main(args: Array<String>) {
	runApplication<BikubeApplication>(*args)
}
