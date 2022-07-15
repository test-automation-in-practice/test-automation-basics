package example.spring.boot.domains.books.model.primitives

private val validRange = 1..10_000

@JvmInline
value class NumberOfPages(val value: Int) {
    init {
        require(value in validRange) {
            "[$value] is not a valid number of pages! (must be between ${validRange.first} and ${validRange.last})"
        }
    }
}
