package example.spring.boot.domains.books.model.primitives

import com.fasterxml.jackson.annotation.JsonValue

data class Title(
    @JsonValue private val value: String
) {
    override fun toString() = value
}
