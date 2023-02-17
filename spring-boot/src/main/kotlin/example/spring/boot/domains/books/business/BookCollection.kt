package example.spring.boot.domains.books.business

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import example.spring.boot.domains.books.model.Book
import example.spring.boot.domains.books.model.BookData
import example.spring.boot.domains.books.model.Roles.CURATOR
import example.spring.boot.domains.books.model.Roles.USER
import example.spring.boot.domains.books.model.State.Available
import example.spring.boot.domains.books.model.State.Borrowed
import example.spring.boot.domains.books.model.events.BookAddedEvent
import example.spring.boot.domains.books.model.events.BookBorrowedEvent
import example.spring.boot.domains.books.model.events.BookDeletedEvent
import example.spring.boot.domains.books.model.events.BookReturnedEvent
import example.spring.boot.domains.books.model.primitives.Borrower
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import org.springframework.util.IdGenerator
import java.time.Clock
import java.util.UUID
import javax.annotation.security.RolesAllowed

/**
 * This is THE central component of the _books_ domain. It provides all kinds of interactions with the managed books.
 * In addition, it is also responsible for enforcing security requirements for its functions.
 */
@Service
class BookCollection(
    private val repository: BookRepository,
    private val eventPublisher: BookEventPublisher,
    private val idGenerator: IdGenerator,
    private val clock: Clock
) {

    private val log = getLogger(javaClass)

    /**
     * Creates a new [Book] with the given [BookData] and returns it.
     * Produces a [BookAddedEvent].
     */
    @RolesAllowed(CURATOR)
    fun addBook(data: BookData): Book {
        log.info("creating new book [$data]")
        val id = idGenerator.generateId()
        val book = Book(id = id, data = data)
        repository.insert(book)
        publishAddedEvent(book)
        log.debug("book with ID [$id] was created")
        return book
    }

    private fun publishAddedEvent(book: Book) {
        eventPublisher.publish(BookAddedEvent(book))
    }

    /**
     * Tries to get a [Book] by its ID.
     * Returns `null` if no such [Book] exists.
     */
    @RolesAllowed(USER)
    fun getBook(id: UUID): Book? {
        log.info("looking up book with ID [$id]")
        val book = repository.get(id)
        when (book) {
            null -> log.debug("book with ID [$id] not found")
            else -> log.debug("book with ID [$id] found")
        }
        return book
    }

    /**
     * Tries to borrow a [Book] by its ID.
     * Returns a failure if there is no such [Book] or it is not in a borrowable state.
     * Produces a [BookBorrowedEvent].
     */
    @RolesAllowed(USER)
    fun borrowBook(id: UUID, borrower: Borrower): Either<BookFailure, Book> {
        log.info("borrowing book with ID [$id]")
        val newState = Borrowed(by = borrower, at = clock.instant())

        val updatedBook = try { // TODO make better
            repository.update(id) { book -> book.changeState(newState) }
        } catch (e: IllegalStateException) {
            return Left(BookUpdateFailed)
        }

        return if (updatedBook != null) {
            publishBorrowedEvent(updatedBook)
            Right(updatedBook)
        } else {
            Left(BookNotFound)
        }
    }

    private fun publishBorrowedEvent(book: Book) {
        eventPublisher.publish(BookBorrowedEvent(book))
    }

    /**
     * Tries to return a [Book] by its ID.
     * Returns a failure if there is no such [Book] or it is not in a returnable state.
     * Produces a [BookReturnedEvent].
     */
    @RolesAllowed(USER)
    fun returnBook(id: UUID): Either<BookFailure, Book> {
        log.info("returning book with ID [$id]")

        val updatedBook = try { // TODO make better
            repository.update(id) { book -> book.changeState(Available) }
        } catch (e: IllegalStateException) {
            return Left(BookUpdateFailed)
        }

        return if (updatedBook != null) {
            publishReturnedEvent(updatedBook)
            Right(updatedBook)
        } else {
            Left(BookNotFound)
        }
    }

    private fun publishReturnedEvent(book: Book) {
        eventPublisher.publish(BookReturnedEvent(book))
    }

    /**
     * Tries to delete a [Book] by its ID.
     * Produces a [BookDeletedEvent] if the book was actually deleted.
     */
    @RolesAllowed(CURATOR)
    fun deleteBook(id: UUID) {
        log.info("deleting book with ID [$id]")
        when (repository.delete(id)) {
            true -> {
                publishDeletedEvent(id)
                log.debug("book successfully deleted")
            }

            false -> log.warn("there is no book with ID [$id] - did not deleted anything")
        }
    }

    private fun publishDeletedEvent(id: UUID) {
        eventPublisher.publish(BookDeletedEvent(id))
    }

}
