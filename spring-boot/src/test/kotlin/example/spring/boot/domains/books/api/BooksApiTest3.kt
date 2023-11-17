package example.spring.boot.domains.books.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import example.spring.boot.Examples
import example.spring.boot.MongoDBInitializer
import example.spring.boot.S3Initializer
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.ContextConfiguration
import java.util.*

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(
    initializers = [
        S3Initializer::class,
        MongoDBInitializer::class,
    ]
)
class BooksApiTest3 @Autowired constructor(private val mapper: ObjectMapper) {

    @BeforeEach
    fun setupRestAssured(@LocalServerPort port: Int) {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
    }

    @Nested
    inner class CreateBook {

        @Test
        fun `creates Book correctly`() {
            Given {
                header(AUTHORIZATION, curatorBasicAuth())
                header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                body(AddBookRequest(Examples.CLEAN_CODE.isbn, Examples.CLEAN_CODE.title))
            } When {
                post("/api/books")
            } Then {
                statusCode(CREATED.value())
            } Extract {
                val result = mapper.readValue<BookRepresentation>(body().asInputStream())

                result.available shouldBe true
                result.borrowed shouldBe null
                result.data shouldBe Examples.CLEAN_CODE
            }
        }

        @Test
        fun `creating Book fails when caller has insufficient rights`() {
            Given {
                header(AUTHORIZATION, userBasicAuth())
                header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                body(AddBookRequest(Examples.CLEAN_CODE.isbn, Examples.CLEAN_CODE.title))
            } When {
                post("/api/books")
            } Then {
                statusCode(FORBIDDEN.value())
            } Extract {
                val result = mapper.readTree(body().asInputStream())

                result.get("timestamp") shouldNotBe null
                result.get("status")?.asText() shouldBe "403"
                result.get("error")?.asText() shouldBe "Forbidden"
                result.get("path")?.asText() shouldBe "/api/books"
            }
        }

        @Test
        fun `creating Book fails when given Isbn is invalid`() {
            Given {
                header(AUTHORIZATION, curatorBasicAuth())
                header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                body("""{"title": "Clean Code", "isbn": "978-01322350884"}""")
            } When {
                post("/api/books")
            } Then {
                statusCode(BAD_REQUEST.value())
            } Extract {
                val result = mapper.readTree(body().asInputStream())

                result.get("timestamp") shouldNotBe null
                result.get("status")?.asText() shouldBe "400"
                result.get("error")?.asText() shouldBe "Bad Request"
                result.get("path")?.asText() shouldBe "/api/books"
            }
        }
    }

    fun curatorBasicAuth() =
        "Basic ${Base64.getEncoder().encodeToString("curator:${"curator".reversed()}".toByteArray())}"

    fun userBasicAuth() =
        "Basic ${Base64.getEncoder().encodeToString("user:${"user".reversed()}".toByteArray())}"

}