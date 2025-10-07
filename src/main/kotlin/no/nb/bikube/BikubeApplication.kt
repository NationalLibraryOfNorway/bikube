package no.nb.bikube

import com.vaadin.flow.server.AppShellSettings
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import com.vaadin.flow.component.page.AppShellConfigurator

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
class BikubeApplication:AppShellConfigurator{
    override fun configurePage(settings: AppShellSettings) {
        settings.setPageTitle("Hugin")
    }
}

fun main(args: Array<String>) {
	runApplication<BikubeApplication>(*args)
}
