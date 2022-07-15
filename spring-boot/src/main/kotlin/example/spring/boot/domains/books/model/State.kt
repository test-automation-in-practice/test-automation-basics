package example.spring.boot.domains.books.model

import example.spring.boot.domains.books.model.primitives.Borrower
import java.time.Instant

sealed interface State {
    object Available : State
    data class Borrowed(val by: Borrower, val at: Instant) : State
}
