package example.spring.boot.domains.books.business

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

const val BUCKET_NAME = "bucket"

@Service
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class BookCoverService(private val s3: AmazonS3) {

    fun findCover(id: UUID): BookCoverData {
        val s3Object: S3Object = s3.getObject(BUCKET_NAME, id.toString())
        return BookCoverData(
            byteStream = s3Object.objectContent,
            contentLength = s3Object.objectMetadata.contentLength,
            contentType = s3Object.objectMetadata.contentType,
        )
    }

    fun saveCover(id: UUID, cover: ByteArray, contentType: String) {
        s3.putObject(
            BUCKET_NAME,
            id.toString(),
            ByteArrayInputStream(cover),
            ObjectMetadata().also {
                it.contentLength = cover.size.toLong()
                it.contentType = contentType
            }
        )
    }
}

data class BookCoverData(
    val byteStream: InputStream,
    val contentLength: Long,
    val contentType: String,
)
