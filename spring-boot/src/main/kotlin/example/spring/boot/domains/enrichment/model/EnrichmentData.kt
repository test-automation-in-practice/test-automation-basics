package example.spring.boot.domains.enrichment.model

import example.spring.boot.domains.books.model.primitives.Author
import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.books.model.primitives.NumberOfPages

data class EnrichmentData(
    val isbn: Isbn,
    val numberOfPages: NumberOfPages?,
    val authors: Set<Author>
)
