package example.spring.boot.domains.books.model.primitives

import com.fasterxml.jackson.annotation.JsonValue

data class Isbn(
    @JsonValue private val value: String
) {
    init {
        require(value matches Regex("""(\d{3}-)?\d{10}""")) {
            "[$value] is not a valid 10 or 13 digit ISBN!"
        }
    }

    override fun toString() = value
}
