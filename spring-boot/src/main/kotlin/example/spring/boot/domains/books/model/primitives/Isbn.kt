package example.spring.boot.domains.books.model.primitives

@JvmInline
value class Isbn(val value: String) {
    init {
        require(value matches Regex("""(\d{3}-)?\d{10}""")) {
            "[$value] is not a valid 10 or 13 digit ISBN!"
        }
    }
}
