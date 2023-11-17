package example.spring.boot

import example.spring.boot.domains.books.model.BookData
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.Title

object Examples {

    val CLEAN_CODE = BookData(
        isbn = Isbn("978-0132350884"),
        title = Title("Clean Code")
    )

}
