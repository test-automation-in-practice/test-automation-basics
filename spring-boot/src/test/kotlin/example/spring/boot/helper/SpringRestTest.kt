package example.spring.boot.helper

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringRestTest: SpringTest() {

    @LocalServerPort
    protected val port: Int = 0

    protected lateinit var rest: TestRestTemplate

    @BeforeEach
    fun setUp() {
        rest = TestRestTemplate(RestTemplateBuilder().rootUri("http://localhost:$port"))
    }
}