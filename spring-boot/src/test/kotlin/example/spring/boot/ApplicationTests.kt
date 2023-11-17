package example.spring.boot

import arrow.core.Either
import arrow.core.getOrHandle
import example.spring.boot.domains.books.api.BookRepresentation
import example.spring.boot.domains.books.business.BookFailure
import example.spring.boot.domains.books.business.BookNotFound
import example.spring.boot.domains.books.business.BookUpdateFailed
import example.spring.boot.domains.books.model.primitives.Borrower
import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.Matchers.oneOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.time.Instant.now
import java.time.temporal.ChronoUnit.SECONDS
import java.util.UUID
import java.util.UUID.randomUUID
import arrow.core.Either.Left as failure
import arrow.core.Either.Right as success

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(
    initializers = [
        S3Initializer::class,
        MongoDBInitializer::class,
    ]
)
class ApplicationTests {

    @LocalServerPort
    fun setupRestAssured(port: Int) {
        RestAssured.port = port
    }

    @Test
    fun `adding books work in general`() {
        addBook("""{ "isbn": "978-0007532278", "title": "I, Robot" }""")
    }

    @Test
    fun `getting non-existing books by their ID works in general`() {
        val found = getBookById(randomUUID())
        assertThat(found).isNull()
    }

    @Test
    fun `getting existing books by their ID works in general`() {
        val created = addBook("""{ "isbn": "978-0356511719", "title": "Brief Cases" }""")
        val found = getBookById(created.id)
        assertThat(found).isEqualTo(created)
    }

    @Test
    fun `borrowing and returning a book works in general`() {
        val book = addBook("""{ "isbn": "978-0593598016", "title": "Fire & Blood" }""")
        assertThat(book.borrowed).isNull()
        assertThat(book.available).isTrue()

        val borrowed = borrowBookById(book.id, "Aegon").getOrThrow()
        assertThat(borrowed.borrowed?.at).isCloseTo(now(), within(5, SECONDS))
        assertThat(borrowed.borrowed?.by).isEqualTo(Borrower("Aegon"))
        assertThat(borrowed.available).isFalse()

        val returned = returnBookById(book.id).getOrThrow()
        assertThat(returned.borrowed).isNull()
        assertThat(returned.available).isTrue()
    }

    @Test
    fun `borrowing and returning invalid books work in general`() {
        val book = addBook("""{ "isbn": "978-0593598016", "title": "Fire & Blood" }""")

        borrowBookById(book.id, "Aegon I").getOrThrow()
        assertThat(borrowBookById(book.id, "Aegon II")).isEqualTo(failure(BookUpdateFailed))

        returnBookById(book.id).getOrThrow()
        assertThat(returnBookById(book.id)).isEqualTo(failure(BookUpdateFailed))
    }

    @Test
    fun `deleting non-existing books works in general`() {
        deleteBookById(randomUUID())
    }

    @Test
    fun `deleting existing books works in general`() {
        val book = addBook("""{ "isbn": "978-0553293357", "title": "Foundation" }""")
        deleteBookById(book.id)
    }

    fun addBook(@Language("json") requestBody: String): BookRepresentation =
        Given {
            asCurator()
            contentType("application/json")
            body(requestBody)
        } When {
            post("/api/books")
        } Then {
            statusCode(201)
        } Extract {
            body().`as`(BookRepresentation::class.java)
        }

    fun getBookById(id: UUID): BookRepresentation? =
        Given {
            asCurator()
        } When {
            get("/api/books/$id")
        } Then {
            statusCode(oneOf(200, 404))
        } Extract {
            when (val status = statusCode()) {
                200 -> body().`as`(BookRepresentation::class.java)
                404 -> null
                else -> error("unexpected status code $status")
            }
        }

    fun borrowBookById(id: UUID, borrower: String): Either<BookFailure, BookRepresentation> =
        Given {
            asCurator()
            contentType("application/json")
            body("""{ "borrower": "$borrower" }""")
        } When {
            post("/api/books/$id/borrow")
        } Then {
            statusCode(oneOf(200, 404, 409))
        } Extract {
            when (val status = statusCode()) {
                200 -> success(value = body().`as`(BookRepresentation::class.java))
                404 -> failure(BookNotFound)
                409 -> failure(BookUpdateFailed)
                else -> error("unexpected status code $status")
            }
        }

    fun returnBookById(id: UUID): Either<BookFailure, BookRepresentation> =
        Given {
            asCurator()
        } When {
            post("/api/books/$id/return")
        } Then {
            statusCode(oneOf(200, 404, 409))
        } Extract {
            when (val status = statusCode()) {
                200 -> success(body().`as`(BookRepresentation::class.java))
                404 -> failure(BookNotFound)
                409 -> failure(BookUpdateFailed)
                else -> error("unexpected status code $status")
            }
        }

    fun deleteBookById(id: UUID) =
        Given {
            asCurator()
        } When {
            delete("/api/books/$id")
        } Then {
            statusCode(204)
        }

    fun RequestSpecification.asCurator() =
        auth().basic("curator", "curator".reversed())

    fun RequestSpecification.asUser() =
        auth().basic("user", "user".reversed())

    fun <T> Either<*, T>.getOrThrow(): T =
        getOrHandle { error("unexpected: $it") }

}
