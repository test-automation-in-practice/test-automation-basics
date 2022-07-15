package example.spring.boot.gateways.bookdataservice

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URL

@ConstructorBinding
@ConfigurationProperties("services.book-data-service")
data class BookDataServiceProperties(
    val baseUrl: URL,
    val credentials: Credentials
) {

    fun url(path: String): URL =
        baseUrl.toURI().resolve(path).toURL()

    @ConstructorBinding
    data class Credentials(val username: String, val password: String)
}
