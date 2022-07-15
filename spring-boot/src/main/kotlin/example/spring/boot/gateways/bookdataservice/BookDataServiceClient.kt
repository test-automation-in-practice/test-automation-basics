package example.spring.boot.gateways.bookdataservice

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import example.spring.boot.common.http.BasicAuthInterceptor
import example.spring.boot.domains.books.model.primitives.Author
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.NumberOfPages
import example.spring.boot.domains.enrichment.business.BookEnrichmentDataRepository
import example.spring.boot.domains.enrichment.model.EnrichmentData
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BookDataServiceClient(
    private val properties: BookDataServiceProperties
) : BookEnrichmentDataRepository {

    private val log = getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authorizationInterceptor(properties))
        .build()

    @Cacheable("book-data-service", unless = "#result == null")
    override fun getByIsbn(isbn: Isbn): EnrichmentData? {
        val url = properties.url("/api/books/$isbn")
        val request = Request.Builder()
            .header(ACCEPT, APPLICATION_JSON_VALUE)
            .get().url(url)
            .build()

        return client.newCall(request).execute()
            .use { response ->
                when (val status = response.code) {
                    200 -> readEnrichmentData(isbn, response)
                    204 -> null
                    else -> {
                        log.warn("unexpected response status [$status] - returning null")
                        log.debug("response body was: ${readBodyAsString(response)}")
                        null
                    }
                }
            }
    }

    private fun readEnrichmentData(isbn: Isbn, response: Response): EnrichmentData {
        val data = objectMapper.readValue<ResponseBody>(response.body!!.byteStream())
        return EnrichmentData(
            isbn = isbn,
            numberOfPages = data.pages?.let(::NumberOfPages),
            authors = data.authors?.map { Author(it.name) }?.toSet() ?: emptySet()
        )
    }

    private fun readBodyAsString(response: Response): String =
        response.body?.string() ?: ""

    private fun authorizationInterceptor(properties: BookDataServiceProperties): BasicAuthInterceptor =
        BasicAuthInterceptor(username = properties.credentials.username, password = properties.credentials.password)

    private data class ResponseBody(val pages: Int?, val authors: Set<Author>?) {
        data class Author(val id: UUID, val name: String)
    }
}
