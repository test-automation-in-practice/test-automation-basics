package example.spring.boot.domains.books.business

import example.spring.boot.domains.books.model.events.BookEvent

interface BookEventPublisher {
    fun publish(event: BookEvent)
}
