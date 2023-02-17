package example.spring.boot.domains.books.model.primitives

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class NumberOfPagesTests {

    // example "2" and "9999" are not necessary but they make @slu feel better

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 9_999, 10_000])
    fun `values within bounds are ok`(example: Int) {
        NumberOfPages(example)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 10_001])
    fun `values without bounds are invalid`(example: Int) {
        val ex = assertThrows<IllegalArgumentException> {
            NumberOfPages(example)
        }
        assertThat(ex).hasMessageContaining("[$example] is not a valid number of pages!")
    }

    // one test case is enough even though someone could break the production code by
    // hard-coding "42" as the response for these methods. But we trust our colleagues :)
    // (and maybe we've come to these tests through TDD and just removed the redundancy)

    @Test
    fun `the string representation is delegated to the original value`() {
        val example = NumberOfPages(42)
        val actual = example.toString()
        assertThat(actual).isEqualTo("42")
    }

    @Test
    fun `the integer representation is the original value`() {
        val examples = NumberOfPages(42)
        val actual = examples.toInt()
        assertThat(actual).isEqualTo(42)
    }

}
