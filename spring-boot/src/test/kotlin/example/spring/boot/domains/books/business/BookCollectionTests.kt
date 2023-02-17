package example.spring.boot.domains.books.business

import example.spring.boot.domains.books.model.events.BookDeletedEvent
import io.github.logrecorder.api.LogRecord
import io.github.logrecorder.assertion.LogRecordAssertion.Companion.assertThat
import io.github.logrecorder.assertion.containsExactly
import io.github.logrecorder.logback.junit5.RecordLoggers
import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.util.IdGenerator
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class BookCollectionTests {

    val repository: BookRepository = mockk()
    val eventPublisher: BookEventPublisher = mockk()
    val idGenerator: IdGenerator = mockk()
    val clock: Clock = Clock.fixed(Instant.parse("2023-02-17T12:34:56.789Z"), ZoneId.of("UTC"))

    val cut = BookCollection(repository, eventPublisher, idGenerator, clock)

    @Nested
    inner class AddingBooks {

    }

    @Nested
    inner class DeletingBooks {

        val id = UUID.randomUUID()
        val eventSlot = slot<BookDeletedEvent>()

        @BeforeEach
        fun defineDefaultBehaviour() {
            every { repository.delete(any()) } returns false
            every { eventPublisher.publish(capture(eventSlot)) } just runs
        }

        @Test
        fun `deleting an existing book publishes an event`() {
            every { repository.delete(id) } returns true
            cut.deleteBook(id)
            assertThat(eventSlot.captured).isEqualTo(BookDeletedEvent(id))
        }

        @Test
        @RecordLoggers(BookCollection::class)
        fun `deleting an existing book logs the deletion`(log: LogRecord) {
            every { repository.delete(id) } returns true
            cut.deleteBook(id)
            assertThat(log) containsExactly {
                info("deleting book with ID [$id]")
                any("book successfully deleted")
            }
        }

        @Test
        fun `deleting a non-existing book does not publishes an event`() {
            every { repository.delete(any()) } returns false
            cut.deleteBook(id)
            verify { eventPublisher wasNot called }
        }

        @Test
        @RecordLoggers(BookCollection::class)
        fun `deleting a non-existing book logs a warning`(log: LogRecord) {
            cut.deleteBook(id)
            assertThat(log) containsExactly {
                info("deleting book with ID [$id]")
                warn("there is no book with ID [$id] - did not deleted anything")
            }
        }

    }

}
