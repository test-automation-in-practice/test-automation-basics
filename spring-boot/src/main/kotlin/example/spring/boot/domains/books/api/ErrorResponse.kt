package example.spring.boot.domains.books.api

import java.time.Instant

data class ErrorResponse(
    val message: String,
    val timestamp: Instant,
)