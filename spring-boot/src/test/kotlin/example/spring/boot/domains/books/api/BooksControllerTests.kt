package example.spring.boot.domains.books.api

import com.ninjasquad.springmockk.MockkBean
import example.spring.boot.common.security.Authorities.SCOPE_API
import example.spring.boot.common.security.WebSecurityConfiguration
import example.spring.boot.domains.books.business.BookCollection
import example.spring.boot.domains.books.model.TestExamples.min_available_book
import example.spring.boot.domains.books.model.TestExamples.min_data
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@MockkBean(BookCollection::class)
@WebMvcTest(BooksController::class)
@Import(WebSecurityConfiguration::class)
@WithMockUser(authorities = [SCOPE_API])
internal class BooksControllerTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val collection: BookCollection
) {

    @Test
    fun `posting book data creates a new book`() {
        every { collection.addBook(min_data) } returns min_available_book

        mockMvc
            .post("/api/books") {
                contentType = APPLICATION_JSON
                content = """
                    {
                      "isbn": "978-0593440414",
                      "title": "Dead Man's Hand"
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
                          "id": "41b97869-90bf-40c7-8858-9dcb74595272",
                          "data": {
                            "isbn": "978-0593440414",
                            "title": "Dead Man's Hand"
                          },
                          "available": true
                        } 
                        """
                    )
                }
            }
    }

}
