package example.spring.boot.domains.books.api

import com.ninjasquad.springmockk.MockkBean
import example.spring.boot.common.security.Authorities.SCOPE_API
import example.spring.boot.common.security.WebSecurityConfiguration
import example.spring.boot.domains.books.business.BookCollection
import example.spring.boot.domains.books.model.Book
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

class BooksControllerTests {

    @Nested
    inner class UnitTest {

    }

    @Nested
    @MockkBean(BookCollection::class)
    @WebMvcTest(BooksController::class)
    @Import(WebSecurityConfiguration::class)
    @WithMockUser(authorities = [SCOPE_API])
    inner class TechnologyIntegrationTests(
        @Autowired val collection: BookCollection,
        @Autowired val mockMvc: MockMvc
    ) {

        @BeforeEach
        fun defineDefaultBehaviour() {
            every { collection.addBook(any()) } answers {
                Book(id = UUID.fromString("cad91353-5cc1-4d97-888a-f116730bb495"), data = firstArg())
            }
        }

        @Test
        fun `creating a new book returns its representation with a status 201 created`() {
            mockMvc
                .post("/api/books") {
                    contentType = APPLICATION_JSON
                    content = """
                    {
                      "isbn": "978-1680680584",
                      "title": "We Are Legion"
                    }
                    """
                }
                .andExpect {
                    status { isCreated() }
                    content {
                        contentType(APPLICATION_JSON)
                        json(
                            """
                        {
                          "id": "cad91353-5cc1-4d97-888a-f116730bb495",
                          "data": {
                            "isbn": "978-1680680584",
                            "title": "We Are Legion"
                          },
                          "available": true
                        }
                        """
                        )
                    }
                }
        }
    }

}
