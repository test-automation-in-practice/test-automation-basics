package example.spring.boot.domains.enrichment.gateways.bookdataservice

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URL

@ConfigurationProperties("services.book-data-service")
data class BookDataServiceProperties(
    val baseUrl: URL,
    val credentials: Credentials
) {

    fun url(path: String): URL =
        baseUrl.toURI().resolve(path).toURL()

    data class Credentials(val username: String, val password: String)
}
