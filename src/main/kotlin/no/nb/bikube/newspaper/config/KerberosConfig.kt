package no.nb.bikube.newspaper.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class KerberosConfig {
    @PostConstruct
    fun setKerberosConfigurationPath() {
        System.setProperty("java.security.krb5.conf", "src/main/resources/krb5.conf")
    }
}