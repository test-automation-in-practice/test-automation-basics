package example.spring.boot.domains.enrichment.business

import example.spring.boot.domains.books.model.primitives.Isbn
import example.spring.boot.domains.enrichment.model.EnrichmentData

interface BookEnrichmentDataRepository {
    fun getByIsbn(isbn: Isbn): EnrichmentData?
}
