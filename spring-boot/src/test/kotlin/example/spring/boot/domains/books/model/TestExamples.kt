package example.spring.boot.domains.books.model

import example.spring.boot.domains.books.model.primitives.Author
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.NumberOfPages
import example.spring.boot.domains.books.model.primitives.Title
import java.util.UUID

object TestExamples {
    val min_data = BookData(
        isbn = Isbn("978-0593440414"),
        title = Title("Dead Man's Hand")
    )
    val min_id = UUID.fromString("41b97869-90bf-40c7-8858-9dcb74595272")
    val min_available_book = Book(
        id = min_id,
        data = min_data,
        state = State.Available
    )

    val max_data = BookData(
        isbn = Isbn("978-3608938197"),
        title = Title("Das Silmarillion"),
        numberOfPages = NumberOfPages(4200),
        authors = setOf(Author("Christopher Tolkien"))
    )
    val max_id = UUID.fromString("3c7a057d-aef6-44c3-87f3-88d96bf3aa07")
    val max_available_book = Book(
        id = max_id,
        data = max_data,
        state = State.Available
    )
}
