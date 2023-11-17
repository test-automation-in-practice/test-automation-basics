package example.spring.boot.domains.books.api

import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.MockkBean
import example.spring.boot.Examples.CLEAN_CODE
import example.spring.boot.common.security.Authorities.ROLE_CURATOR
import example.spring.boot.common.security.Authorities.SCOPE_API
import example.spring.boot.common.security.WebSecurityConfiguration
import example.spring.boot.domains.books.business.BookCollection
import example.spring.boot.domains.books.business.BookCoverService
import example.spring.boot.domains.books.business.BookUpdateFailed
import example.spring.boot.domains.books.model.Book
import example.spring.boot.domains.books.model.State.Available
import io.mockk.every
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID.randomUUID

@WebMvcTest(BooksController::class)
@MockkBean(classes = [BookCollection::class, BookCoverService::class])
@Import(WebSecurityConfiguration::class, TestConfig::class)
class BooksApiTest2 @Autowired constructor(
    private val mvc: MockMvc,
    private val bookCollection: BookCollection,
) {

    @Nested
    inner class ReturnBook {

        private val id = randomUUID()

        @Test
        @WithMockUser(authorities = [ROLE_CURATOR, SCOPE_API])
        fun `returns Book successfully when it borrowed`() {
            val book = Book(id = id, data = CLEAN_CODE, state = Available)
            every { bookCollection.returnBook(id) } returns book.right()

            mvc.post("/api/books/$id/return")
                .andExpect {
                    status {
                        isOk()
                    }
                    content {
                        json(
                            """
                            {
                              "id": "$id",
                              "data": {
                                "isbn": "${CLEAN_CODE.isbn}",
                                "title": "${CLEAN_CODE.title}",
                                "authors": []
                              },
                              "available": true
                            }""".trimIndent(),
                            strict = true
                        )
                    }
                }
        }

        @Test
        @WithMockUser(authorities = [ROLE_CURATOR, SCOPE_API])
        fun `fails to return Book when it is not borrowed`() {
            every { bookCollection.returnBook(id) } returns BookUpdateFailed.left()

            mvc.post("/api/books/$id/return")
                .andExpect {
                    status {
                        isConflict()
                    }
                    content {
                        json(
                            """
                            {
                              "message": "Book with id [$id] is not borrowed",
                              "timestamp": "${TestConfig.NOW}"
                            }""".trimIndent(),
                            strict = true
                        )
                    }
                }
        }
    }
}

private class TestConfig {

    companion object {
        const val NOW = "2020-01-08T13:00:00Z"
    }

    @Bean
    fun clock(): Clock = Clock.fixed(Instant.parse(NOW), ZoneId.systemDefault())
}