package no.nb.bikube.newspaper.config

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("kerberos")
class KerberosConfigProperties (
    var realm: String,
    var kdc: String
)

@Configuration
class KerberosConfig(
    private val krb5: KerberosConfigProperties
) {
    @PostConstruct
    fun setKerberosConfigurationPath() {
        System.setProperty("java.security.krb5.realm", krb5.realm)
        System.setProperty("java.security.krb5.kdc", krb5.kdc)
    }
}