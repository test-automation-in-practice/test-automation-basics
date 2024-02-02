package example.spring.boot.domains.books.business

import com.amazonaws.services.s3.model.AmazonS3Exception
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.MediaType.TEXT_XML_VALUE
import java.util.UUID.randomUUID

class BookCoverServiceTest {

    private val s3 = MockS3()
    private val cut = BookCoverService(s3)

    private val bytes = "123".toByteArray()

    @Test
    fun `fails when there are no covers`() {
        val error = shouldThrow<AmazonS3Exception> { cut.findCover(randomUUID()) }

        error.statusCode shouldBe 404
    }

    @Test
    fun `fails when there is no cover for the given id`() {
        val id = randomUUID()
        val otherId = randomUUID()
        cut.saveCover(id, bytes, TEXT_PLAIN_VALUE)

        val error = shouldThrow<AmazonS3Exception> { cut.findCover(otherId) }

        error.statusCode shouldBe 404
    }

    @Test
    fun `correctly finds saved cover`() {
        val id = randomUUID()
        cut.saveCover(id, bytes, TEXT_PLAIN_VALUE)

        val result = cut.findCover(id)

        result.contentType shouldBe TEXT_PLAIN_VALUE
        result.contentLength shouldBe 3
        result.byteStream.readAllBytes() shouldBe bytes
    }

    @Test
    fun `correctly updates saved cover`() {
        val id = randomUUID()
        val newBytes = "45678".toByteArray()
        cut.saveCover(id, bytes, TEXT_PLAIN_VALUE)
        cut.saveCover(id, newBytes, TEXT_XML_VALUE)

        val result = cut.findCover(id)

        assertThat(result).isNotNull()
        result.contentType shouldBe TEXT_XML_VALUE
        result.contentLength shouldBe newBytes.size
        result.byteStream.readAllBytes() shouldBe newBytes
    }
}

