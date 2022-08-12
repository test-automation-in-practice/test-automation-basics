package example.spring.boot.domains.enrichment.gateways.bookdataservice

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(BookDataServiceProperties::class)
class BookDataServiceConfiguration {

    @Bean
    fun bookDataServiceCache(): Cache =
        ConcurrentMapCache("book-data-service")

}
