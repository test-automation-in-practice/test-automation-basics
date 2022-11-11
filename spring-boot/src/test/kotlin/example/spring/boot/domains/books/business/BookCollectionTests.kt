package example.spring.boot.domains.books.business

import example.spring.boot.domains.books.model.Book
import example.spring.boot.domains.books.model.TestExamples.max_available_book
import example.spring.boot.domains.books.model.TestExamples.max_data
import example.spring.boot.domains.books.model.TestExamples.max_id
import example.spring.boot.domains.books.model.TestExamples.min_available_book
import example.spring.boot.domains.books.model.TestExamples.min_data
import example.spring.boot.domains.books.model.TestExamples.min_id
import example.spring.boot.domains.books.model.events.BookAddedEvent
import example.spring.boot.domains.books.model.events.BookEvent
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.util.IdGenerator
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BookCollectionTests {

    val repository: BookRepository = mockk()
    val eventPublisher: BookEventPublisher = mockk()
    val idGenerator: IdGenerator = mockk()
    val clock = Clock.fixed(Instant.parse("2022-11-11T12:34:56.789Z"), ZoneId.of("UTC"))

    val cut = BookCollection(repository, eventPublisher, idGenerator, clock)

    @Nested
    inner class AddingBooks {

        val insertedBook = slot<Book>()
        val publishedEvent = slot<BookEvent>()

        @BeforeEach
        fun stubDefaultBehaviour() {
            every { repository.insert(capture(insertedBook)) } just runs
            every { eventPublisher.publish(capture(publishedEvent)) } just runs
        }

        @Test
        fun `adding a new book returns the the created instance`() {
            every { idGenerator.generateId() } returns min_id
            val actualBook = cut.addBook(min_data)
            assertThat(actualBook).isEqualTo(min_available_book)
        }

        @Test
        fun `adding a new book inserts it into the repository`() {
            every { idGenerator.generateId() } returns max_id
            cut.addBook(max_data)
            assertThat(insertedBook.captured).isEqualTo(max_available_book)
        }

        @Test
        fun `adding a new book publishes an event`() {
            every { idGenerator.generateId() } returns max_id
            cut.addBook(max_data)
            assertThat(publishedEvent.captured).isEqualTo(BookAddedEvent(max_available_book))
        }
    }
}
