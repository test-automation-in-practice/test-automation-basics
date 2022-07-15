package example.spring.boot.domains.enrichment.business

import example.spring.boot.domains.books.business.BookRepository
import example.spring.boot.domains.books.model.Book
import example.spring.boot.domains.books.model.events.BookAddedEvent
import example.spring.boot.domains.enrichment.model.EnrichmentData
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class EnrichAddedBookEventListener(
    private val dataRepository: BookEnrichmentDataRepository,
    private val bookRepository: BookRepository
) {

    @Async
    @EventListener
    fun handle(event: BookAddedEvent) {
        val data = getEnrichmentData(event)
        if (data != null) {
            updateBook(event, data)
        }
    }

    private fun getEnrichmentData(event: BookAddedEvent) =
        dataRepository.getByIsbn(event.book.data.isbn)

    private fun updateBook(event: BookAddedEvent, data: EnrichmentData) =
        bookRepository.update(event.book.id) { book -> book.merge(data) }

    private fun Book.merge(data: EnrichmentData) =
        copy(data = this.data.copy(numberOfPages = data.numberOfPages, authors = data.authors))

}
