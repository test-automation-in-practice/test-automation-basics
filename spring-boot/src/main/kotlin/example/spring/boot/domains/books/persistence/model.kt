package example.spring.boot.domains.books.persistence

import example.spring.boot.domains.books.model.BookData
import example.spring.boot.domains.books.model.State
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collation = "books")
data class BookDocument(
    @Id
    val id: UUID,
    var data: BookData,
    var borrowed: State.Borrowed?,
    val created: Instant,
    var lastUpdated: Instant
)
