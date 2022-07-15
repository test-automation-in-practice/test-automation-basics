package example.spring.boot.domains.books.business

import example.spring.boot.domains.books.model.Book
import java.util.UUID

interface BookRepository {
    fun insert(book: Book)
    fun get(id: UUID): Book?
    fun update(id: UUID, block: (Book) -> Book): Book?
    fun delete(id: UUID): Boolean
}
