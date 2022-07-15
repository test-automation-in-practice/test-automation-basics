package example.spring.boot.domains.books.business

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

@Service
class BookCollection(
    private val repository: BookRepository,
    private val eventPublisher: BookEventPublisher,
    private val idGenerator: IdGenerator,
    private val clock: Clock
) {

    private val log = getLogger(javaClass)

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

    @RolesAllowed(USER)
    fun borrowBook(id: UUID, borrower: Borrower): Book? {
        log.info("borrowing book with ID [$id]")
        val newState = Borrowed(by = borrower, at = clock.instant())
        val updatedBook = repository.update(id) { book ->
            book.changeState(newState)
        }
        publishBorrowedEvent(updatedBook)
        return updatedBook
    }

    private fun publishBorrowedEvent(book: Book?) {
        if (book != null) eventPublisher.publish(BookBorrowedEvent(book))
    }

    @RolesAllowed(USER)
    fun returnBook(id: UUID): Book? {
        log.info("returning book with ID [$id]")
        val updatedBook = repository.update(id) { book ->
            book.changeState(Available)
        }
        publishReturnedEvent(updatedBook)
        return updatedBook
    }

    private fun publishReturnedEvent(book: Book?) {
        if (book != null) eventPublisher.publish(BookReturnedEvent(book))
    }

    @RolesAllowed(CURATOR)
    fun deleteBook(id: UUID) {
        log.info("deleting book with ID [$id]")
        when (repository.delete(id)) {
            true -> {
                publishDeletedEvent(id)
                log.debug("book successfully deleted")
            }
            else -> log.warn("there is no book with ID [$id] - did not deleted anything")
        }
    }

    private fun publishDeletedEvent(id: UUID) {
        eventPublisher.publish(BookDeletedEvent(id))
    }

}
