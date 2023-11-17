package example.spring.boot.domains.books.model.primitives

import com.fasterxml.jackson.annotation.JsonValue

data class Title(
    @JsonValue private val value: String
) {

    init {
        require(value.isNotBlank()) { "Title must not be blank!" }
    }

    override fun toString() = value
}
