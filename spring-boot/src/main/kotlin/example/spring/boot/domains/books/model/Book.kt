package example.spring.boot.domains.books.model

import example.spring.boot.domains.books.model.State.Available
import example.spring.boot.domains.books.model.State.Borrowed
import java.util.UUID

data class Book(
    val id: UUID,
    val data: BookData,
    val state: State = Available
) {

    fun changeState(newState: State): Book {
        when (newState) {
            is Available -> check(state is Borrowed) { "book with ID [$id] is not borrowed!" }
            is Borrowed -> check(state is Available) { "book with ID [$id] is not available!" }
        }
        return copy(state = newState)
    }

}
