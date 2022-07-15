package example.spring.boot.domains.books.model

import example.spring.boot.domains.books.model.primitives.Author
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.NumberOfPages
import example.spring.boot.domains.books.model.primitives.Title

data class BookData(
    val isbn: Isbn,
    val title: Title,
    val numberOfPages: NumberOfPages? = null,
    val authors: Set<Author> = emptySet()
)
