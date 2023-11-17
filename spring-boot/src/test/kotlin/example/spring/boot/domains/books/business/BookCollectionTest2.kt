package example.spring.boot.domains.books.business

import arrow.core.Either
import example.spring.boot.domains.books.model.Book
import example.spring.boot.domains.books.model.BookData
import example.spring.boot.domains.books.model.State
import example.spring.boot.domains.books.model.events.BookAddedEvent
import example.spring.boot.domains.books.model.events.BookBorrowedEvent
import example.spring.boot.domains.books.model.primitives.Author
import example.spring.boot.domains.books.model.primitives.Borrower
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.NumberOfPages
import example.spring.boot.domains.books.model.primitives.Title
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.util.IdGenerator
import java.time.Clock
import java.time.Instant
import java.util.UUID.randomUUID

internal class BookCollectionTest2 {

    private val idGenerator: IdGenerator = mockk()
    private val repository: BookRepository = mockk()
    private val eventPublisher: BookEventPublisher = mockk(relaxUnitFun = true)
    private val clock: Clock = mockk()

    private val cut = BookCollection(repository, eventPublisher, idGenerator, clock)

    @Nested
    @DisplayName("Add Book")
    inner class AddBook {
        private val id = randomUUID()
        private val bookData = BookData(
            Isbn("1234567890"),
            Title("Lord of the Rings"),
            NumberOfPages(123),
            setOf(Author("JRR Tolkien"))
        )

        @BeforeEach
        fun setup() {
            every { idGenerator.generateId() } returns id
            every { eventPublisher.publish(any()) } just runs
            every { repository.insert(any()) } just runs
        }

        @Nested
        inner class Successes {

            @Test
            fun `adding book returns book data`() {
                cut.addBook(bookData).let { result ->
                    result.id shouldBe id
                    result.data.title shouldBe bookData.title
                    result.data.isbn shouldBe bookData.isbn
                    result.data.numberOfPages shouldBe bookData.numberOfPages
                    result.data.authors shouldBe bookData.authors
                }
            }

            @Test
            fun `adding book publishes BookAdded event`() {
                cut.addBook(bookData)
                verify { eventPublisher.publish(BookAddedEvent(Book(id, bookData))) }
            }

            @Test
            fun `adding book inserts data into database`() {
                cut.addBook(bookData)
                verify { repository.insert(Book(id, bookData)) }
            }
        }

        @Nested
        inner class Failures {

            @Test
            fun `fails when BookAdded event cannot be published`() {
                every { eventPublisher.publish(any()) } throws Exception("ERROR")

                val error = shouldThrow<Exception> { cut.addBook(bookData) }

                error shouldHaveMessage "ERROR"
            }

            @Test
            fun `fails when book data cannot be saved`() {
                every { repository.insert(any()) } throws Exception("ERROR")

                val error = shouldThrow<Exception> { cut.addBook(bookData) }

                error shouldHaveMessage "ERROR"
            }
        }
    }

    @Nested
    @DisplayName("Borrow Book")
    inner class BorrowBook {

        private val bookId = randomUUID()
        private val gandalf = Borrower("Gandalf")
        private val book = Book(
            id = bookId,
            data = BookData(
                Isbn("1234567890"),
                Title("Lord of the Rings"),
                NumberOfPages(123),
                setOf(Author("JRR Tolkien"))
            )
        )
        private val now = Instant.parse("2023-07-21T14:39:42Z")

        @BeforeEach
        fun setup() {
            every { clock.instant() } returns now
            every { repository.update(bookId, any()) } answers {
                return@answers secondArg<(Book) -> Book>()(book)
            }
            every { eventPublisher.publish(BookBorrowedEvent(book)) } just runs
        }

        @Nested
        inner class Successes {

            @Test
            fun `borrowing book returns updated book data`() {
                val result: Either<BookFailure, Book> = cut.borrowBook(bookId, gandalf)

                result shouldBeRightAnd { updatedBook ->
                    updatedBook.id shouldBe bookId
                    updatedBook.data shouldBe updatedBook.data
                    updatedBook.state shouldBe State.Borrowed(gandalf, now)
                }
            }

            @Test
            fun `borrowing book publishes BookBorrowedEvent`() {
                every { repository.update(bookId, any()) } answers {
                    val update = secondArg<(Book) -> Book>()
                    update(book)
                }

                val result: Either<BookFailure, Book> = cut.borrowBook(bookId, gandalf)

                result shouldBeRightAnd { updatedBook ->
                    verify { eventPublisher.publish(BookBorrowedEvent(updatedBook)) }
                }

            }
        }

        @Nested
        inner class Failures {

            @Test
            fun `does not publish BookBorrowedEvent when book does not exist`() {
                every { repository.update(bookId, any()) } returns null

                val result: Either<BookFailure, Book> = cut.borrowBook(bookId, gandalf)

                result shouldBeLeftAnd { failure ->
                    failure.shouldBeInstanceOf<BookNotFound>()
                    verify { eventPublisher wasNot called }
                }
            }
        }
    }
}

infix fun <L, R> Either<L, R>.shouldBeRightAnd(block: (R) -> Unit) {
    this.isRight() shouldBe true
    this.onRight(block)
}

infix fun <L, R> Either<L, R>.shouldBeLeftAnd(block: (L) -> Unit) {
    this.isLeft() shouldBe true
    this.onLeft(block)
}
