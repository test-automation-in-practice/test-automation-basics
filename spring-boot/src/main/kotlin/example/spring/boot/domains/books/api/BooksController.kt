package example.spring.boot.domains.books.api

import example.spring.boot.domains.books.business.BookCollection
import example.spring.boot.domains.books.model.BookData
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/books")
class BooksController(
    private val collection: BookCollection
) {

    private val includeBorrowed = true

    @PostMapping
    @ResponseStatus(CREATED)
    fun addBook(@RequestBody request: AddBookRequest): BookRepresentation {
        val bookData = BookData(isbn = request.isbn, title = request.title)
        val book = collection.addBook(bookData)
        return book.toRepresentation(includeBorrowed)
    }

    @GetMapping("/{id}")
    fun getBookById(@PathVariable id: UUID): BookRepresentation? {
        val book = collection.getBook(id = id)
        return book?.toRepresentation(includeBorrowed)
    }

    @PostMapping("/{id}/borrow")
    fun borrowBookById(@PathVariable id: UUID, @RequestBody request: BorrowBookRequest): BookRepresentation? {
        val book = collection.borrowBook(id = id, borrower = request.borrower)
        return book?.toRepresentation(includeBorrowed)
    }

    @PostMapping("/{id}/return")
    fun returnBookById(@PathVariable id: UUID): BookRepresentation? {
        val book = collection.returnBook(id = id)
        return book?.toRepresentation(includeBorrowed)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun deleteBookById(@PathVariable id: UUID) {
        collection.deleteBook(id = id)
    }

}
