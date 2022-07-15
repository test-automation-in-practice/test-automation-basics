package example.spring.boot.common.events

import example.spring.boot.domains.books.business.BookEventPublisher
import example.spring.boot.domains.books.model.events.BookEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class InMemoryEventPublisher(
    private val publisher: ApplicationEventPublisher
) : BookEventPublisher {
    override fun publish(event: BookEvent) = publisher.publishEvent(event)
}
