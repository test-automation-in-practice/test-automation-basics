package example.spring.boot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    SpringApplicationBuilder(Application::class.java)
        .initializers(S3Initializer(), MongoDBInitializer())
        .run(*args)
}
