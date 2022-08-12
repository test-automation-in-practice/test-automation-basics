package example.spring.boot.domains.books.model.primitives

import com.fasterxml.jackson.annotation.JsonValue

private val validRange = 1..10_000

data class NumberOfPages(
    @JsonValue private val value: Int
) {
    init {
        require(value in validRange) {
            "[$value] is not a valid number of pages! (must be between ${validRange.first} and ${validRange.last})"
        }
    }

    fun toInt(): Int = value
    override fun toString() = value.toString()
}
