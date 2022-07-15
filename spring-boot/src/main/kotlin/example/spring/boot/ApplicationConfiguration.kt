package example.spring.boot

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import java.time.Clock

@EnableAsync
@EnableCaching
@Configuration
class ApplicationConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun idGenerator(): IdGenerator = JdkIdGenerator()

    @Bean
    fun cacheManager(caches: List<Cache>): CacheManager =
        SimpleCacheManager().apply { setCaches(caches) }

}
