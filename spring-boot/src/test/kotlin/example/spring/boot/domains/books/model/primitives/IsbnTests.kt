package example.spring.boot.domains.books.model.primitives

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class IsbnTests {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "1234567890", "5678901234",
            "123-1234567890", "321-6789012345"
        ]
    )
    fun `10 and 13 digit ISBN values are valid`(example: String) {
        assertDoesNotThrow { Isbn(example) }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "12-1234567890", // prefix too short
            "1234-1234567890", // prefix too long
            "12345678901", // short format too long (11)
            "123456789", // short format too short (9)
        ]
    )
    fun `invalid examples`(example: String) {
        val ex = assertThrows<IllegalArgumentException> { Isbn(example) }
        assertThat(ex).hasMessage("[$example] is not a valid 10 or 13 digit ISBN!")
    }
}
