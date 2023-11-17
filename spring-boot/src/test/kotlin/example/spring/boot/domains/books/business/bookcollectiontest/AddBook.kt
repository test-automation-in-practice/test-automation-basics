package example.spring.boot.domains.books.business.bookcollectiontest

import example.spring.boot.domains.books.business.BookCollection
import example.spring.boot.domains.books.business.BookEventPublisher
import example.spring.boot.domains.books.business.BookRepository
import example.spring.boot.domains.books.model.Book
import example.spring.boot.domains.books.model.BookData
import example.spring.boot.domains.books.model.events.BookAddedEvent
import example.spring.boot.domains.books.model.primitives.Author
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.NumberOfPages
import example.spring.boot.domains.books.model.primitives.Title
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.springframework.util.IdGenerator
import java.time.Clock
import java.util.UUID.randomUUID

internal class AddBook {

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
            idGenerator = mock(IdGenerator::class.java)
            repository = mock(BookRepository::class.java)
            eventPublisher = mock(BookEventPublisher::class.java)
            clock = mock(Clock::class.java)
            cut = BookCollection(repository, eventPublisher, idGenerator, clock)
        }
    }

    @AfterEach
    fun resetMocks() {
        Mockito.reset(idGenerator, repository, eventPublisher, clock)
    }

    // region happy path

    @Test
    fun test_addbookhappy() {
        val bookData = BookData(
            Isbn("1234567890"), Title("Lord of the Rings"),
            NumberOfPages(123), setOf(Author("JRR Tolkien"))
        )
        val id = randomUUID()
        `when`(idGenerator.generateId()).thenReturn(id)
        doNothing().`when`(eventPublisher).publish(anyObject())
        doNothing().`when`(repository).insert(anyObject())
        val book = cut.addBook(bookData)
        Assertions.assertEquals(book.id, id)
        Assertions.assertEquals(book.data.title, bookData.title)
        Assertions.assertEquals(book.data.isbn, bookData.isbn)
        Assertions.assertEquals(book.data.numberOfPages, bookData.numberOfPages)
        Assertions.assertEquals(book.data.authors, bookData.authors)
    }

    @Test
    fun test_addbookhappy2() {
        val bookData = BookData(
            Isbn("1234567890"), Title("Lord of the Rings"),
            NumberOfPages(123), setOf(Author("JRR Tolkien"))
        )
        val id = randomUUID()
        `when`(idGenerator.generateId()).thenReturn(id)
        doNothing().`when`(eventPublisher).publish(anyObject())
        doNothing().`when`(repository).insert(anyObject())
        val book = cut.addBook(bookData)
        Assertions.assertEquals(book.id, id)
        Assertions.assertEquals(book.data.title, bookData.title)
        Assertions.assertEquals(book.data.isbn, bookData.isbn)
        Assertions.assertEquals(book.data.numberOfPages, bookData.numberOfPages)
        Assertions.assertEquals(book.data.authors, bookData.authors)
        verify(eventPublisher).publish(BookAddedEvent(Book(id, bookData)))
    }

    @Test
    fun test_addbookhappy3() {
        val bookData = BookData(
            Isbn("1234567890"), Title("Lord of the Rings"),
            NumberOfPages(123), setOf(Author("JRR Tolkien"))
        )
        val id = randomUUID()
        `when`(idGenerator.generateId()).thenReturn(id)
        doNothing().`when`(eventPublisher).publish(anyObject())
        doNothing().`when`(repository).insert(anyObject())
        val book = cut.addBook(bookData)
        Assertions.assertEquals(book.id == id, true)
        Assertions.assertEquals(book.data.title, bookData.title)
        Assertions.assertEquals(book.data.isbn, bookData.isbn)
        Assertions.assertEquals(book.data.numberOfPages, bookData.numberOfPages)
        Assertions.assertEquals(book.data.authors, bookData.authors)
        verify(repository).insert(Book(id, bookData))
    }

    // endregion

    // region bad path

    @Test
    fun test_addbook_bad() {
        try {
            val bookData = BookData(
                Isbn("1234567890"), Title("Lord of the Rings"),
                NumberOfPages(123), setOf(Author("JRR Tolkien"))
            )
            val id = randomUUID()
            `when`(idGenerator.generateId()).thenReturn(id)
            doNothing().`when`(repository).insert(anyObject())
            `when`(
                eventPublisher.publish(
                    BookAddedEvent(
                        Book(
                            id = id,
                            data = bookData
                        )
                    )
                )
            ).thenAnswer { throw RuntimeException("ERROR") }
            cut.addBook(bookData)
            throw Exception("Exception was not thrown")
        } catch (e: Exception) {
            Assertions.assertEquals("ERROR", e.message)
        }
    }

    @Test
    fun test_addbook_bad2() {
        try {
            val bookData = BookData(
                Isbn("1234567890"),
                Title("Lord of the Rings"),
                NumberOfPages(123),
                setOf(Author("JRR Tolkien"))
            )
            val id = randomUUID()
            `when`(idGenerator.generateId()).thenReturn(id)
            doNothing().`when`(eventPublisher).publish(anyObject())
            `when`(repository.insert(Book(id = id, data = bookData))).thenThrow(RuntimeException("ERROR"))
            cut.addBook(bookData)
            throw Exception("Exception was not thrown")
        } catch (e: Exception) {
            verifyNoMoreInteractions(eventPublisher)
            Assertions.assertEquals("ERROR", e.message)
        }
    }

    // endregion

    private fun <T> anyObject(): T {
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

}
