package example.spring.boot.domains.books.model.events

import example.spring.boot.domains.books.model.Book
import java.util.UUID

sealed interface BookEvent

data class BookAddedEvent(val book: Book) : BookEvent
data class BookDeletedEvent(val bookId: UUID) : BookEvent

sealed interface BookUpdatedEvent : BookEvent {
    val book: Book
}

data class BookBorrowedEvent(override val book: Book) : BookUpdatedEvent
data class BookReturnedEvent(override val book: Book) : BookUpdatedEvent
