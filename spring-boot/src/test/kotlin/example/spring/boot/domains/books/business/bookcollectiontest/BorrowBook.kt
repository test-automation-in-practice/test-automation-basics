package example.spring.boot.domains.books.business.bookcollectiontest

import arrow.core.Either
import example.spring.boot.domains.books.business.BookCollection
import example.spring.boot.domains.books.business.BookEventPublisher
import example.spring.boot.domains.books.business.BookFailure
import example.spring.boot.domains.books.business.BookNotFound
import example.spring.boot.domains.books.business.BookRepository
import example.spring.boot.domains.books.model.Book
import example.spring.boot.domains.books.model.BookData
import example.spring.boot.domains.books.model.State
import example.spring.boot.domains.books.model.events.BookBorrowedEvent
import example.spring.boot.domains.books.model.primitives.Author
import example.spring.boot.domains.books.model.primitives.Borrower
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.NumberOfPages
import example.spring.boot.domains.books.model.primitives.Title
import io.mockk.called
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.util.IdGenerator
import java.time.Clock
import java.time.Instant
import java.util.UUID.randomUUID

internal class BorrowBook {

    companion object {
        @JvmStatic
        lateinit var idGenerator: IdGenerator

        @JvmStatic
        lateinit var repository: BookRepository

        @JvmStatic
        lateinit var eventPublisher: BookEventPublisher

        @JvmStatic
        lateinit var clock: Clock

        @JvmStatic
        lateinit var cut: BookCollection

        @JvmStatic
        @BeforeAll
        fun create() {
            idGenerator = mockk<IdGenerator>()
            repository = mockk<BookRepository>()
            eventPublisher = mockk<BookEventPublisher>()
            clock = mockk<Clock>()
            cut = BookCollection(repository, eventPublisher, idGenerator, clock)
        }
    }

    @AfterEach
    fun teardown() {
        clearMocks(idGenerator, repository, eventPublisher, clock)
    }

    // region borrow book

    @Test
    fun test_borrowbook_happy() {
        val bookId = randomUUID()
        val borrower = Borrower("Gandalf")
        val book = Book(
            id = bookId,
            data = BookData(
                Isbn("1234567890"), Title("Lord of the Rings"),
                NumberOfPages(123), setOf(Author("JRR Tolkien"))
            )
        )
        every { clock.instant() } returns Instant.parse("2023-07-21T14:39:42Z")
        every { repository.update(bookId, any()) } answers {
            val update = secondArg<(Book) -> Book>()
            update(book)
        }
        every { eventPublisher.publish(any()) } just runs
        val result = cut.borrowBook(bookId, borrower)
        Assertions.assertTrue(result is Either.Right)
        result as Either.Right<Book>
        Assertions.assertEquals(result.value.id, bookId)
        Assertions.assertEquals(result.value.data, book.data)
        Assertions.assertEquals(
            result.value.state,
            State.Borrowed(
                Borrower("Gandalf"),
                Instant.parse("2023-07-21T14:39:42Z")
            )
        )
    }

    @Test
    fun test_borrowbook_happy2() {
        val bookId = randomUUID()
        val borrower = Borrower("Gandalf")
        var updatedBook: Book? = null
        val book = Book(
            id = bookId,
            data = BookData(
                Isbn("1234567890"), Title("Lord of the Rings"),
                NumberOfPages(123), setOf(Author("JRR Tolkien"))
            )
        )
        every { clock.instant() } returns Instant.parse("2023-07-21T14:39:42Z")
        every { repository.update(bookId, any()) } answers {
            val update = secondArg<(Book) -> Book>()
            updatedBook = update(book)
            updatedBook
        }
        every { eventPublisher.publish(any()) } just runs

        val result = cut.borrowBook(bookId, borrower)
        Assertions.assertTrue(result is Either.Right)
        result as Either.Right<Book>
        Assertions.assertEquals(result.value.id, bookId)
        Assertions.assertEquals(result.value.data, book.data)
        Assertions.assertEquals(
            result.value.state,
            State.Borrowed(
                Borrower("Gandalf"),
                Instant.parse("2023-07-21T14:39:42Z")
            )
        )
        verify { eventPublisher.publish(BookBorrowedEvent(updatedBook!!)) }
    }

    // endregion

    // region bad path

    @Test
    fun test_borrowbook_bad() {
        val bookId = randomUUID()
        val borrower = Borrower("Gandalf")
        val book = Book(
            id = bookId,
            data = BookData(
                Isbn("1234567890"), Title("Lord of the Rings"),
                NumberOfPages(123), setOf(Author("JRR Tolkien"))
            )
        )
        every { clock.instant() } returns Instant.parse("2023-07-21T14:39:42Z")
        every { repository.update(bookId, any()) } returns null
        every { eventPublisher.publish(BookBorrowedEvent(book)) } just runs
        val result = cut.borrowBook(bookId, borrower)
        Assertions.assertTrue(result is Either.Left)
        result as Either.Left<BookFailure>
        Assertions.assertTrue(result.value is BookNotFound)
        verify { eventPublisher wasNot called }
    }

    // endregion
}
