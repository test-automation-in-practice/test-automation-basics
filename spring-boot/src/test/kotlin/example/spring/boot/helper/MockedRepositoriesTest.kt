package example.spring.boot.helper

import com.amazonaws.services.s3.AmazonS3
import com.ninjasquad.springmockk.MockkBean
import example.spring.boot.domains.books.business.BookEventPublisher
import example.spring.boot.domains.books.business.BookRepository

open class MockedRepositoriesTest {

    @MockkBean
    protected lateinit var bookRepository: BookRepository

    @MockkBean
    protected lateinit var bookEventPublisher: BookEventPublisher

    @MockkBean(relaxed = true)
    protected lateinit var s3: AmazonS3
}
