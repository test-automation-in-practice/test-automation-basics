package example.spring.boot.common.events

import example.spring.boot.domains.books.business.BookEventPublisher
import example.spring.boot.domains.books.model.events.BookEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Since this project is only an example, events are published "in-memory" instead of
 * using something like RabbitMQ or Kafka.
 *
 * Events are published directly as _Application Events_ and can be handled by other
 * components using `@EventListener` methods.
 */
@Component
class InMemoryEventPublisher(
    private val publisher: ApplicationEventPublisher
) : BookEventPublisher {
    override fun publish(event: BookEvent) = publisher.publishEvent(event)
}
