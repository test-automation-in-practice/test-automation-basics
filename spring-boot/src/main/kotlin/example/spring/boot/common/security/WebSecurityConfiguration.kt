package example.spring.boot.common.security

import example.spring.boot.common.security.Authorities.SCOPE_ACTUATOR
import example.spring.boot.common.security.Authorities.SCOPE_API
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.toAnyEndpoint
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.HttpSecurityDsl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

/**
 * Enable general web security filters.
 * These manage the principle ability of a given user to make requests on certain resources.
 *
 * In a separate configuration class in order to be able to selectively activate this in tests.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(UsersProperties::class)
class WebSecurityConfiguration {

    private val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    /** Configuration for any `/api` resources. */
    @Bean
    @Order(101) // highest priority
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/api/**")

            defaults()

            httpBasic {}
            authorizeRequests {
                authorize("/api/**", hasAuthority(SCOPE_API))
            }
        }
        return http.build()
    }

    /** Configuration for any `/actuator` resources. */
    @Bean
    @Order(102) // slightly lower priority
    fun actuatorSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher(toAnyEndpoint())

            defaults()

            httpBasic {}
            authorizeRequests {
                authorize(EndpointRequest.to(InfoEndpoint::class.java), permitAll)
                authorize(EndpointRequest.toAnyEndpoint(), hasAuthority(SCOPE_ACTUATOR))
            }
        }
        return http.build()
    }

    /** Configuration for any other resources. */
    @Bean
    @Order(199) // lowest priority
    fun generalSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/**")

            defaults()

            authorizeRequests {
                authorize("/error", permitAll)
                authorize("/**", denyAll)
            }
        }
        return http.build()
    }

    private fun HttpSecurityDsl.defaults() {
        cors { disable() } // not needed for this showcase
        csrf { disable() } // not needed for this showcase
        headers { cacheControl {} }
        sessionManagement { sessionCreationPolicy = STATELESS } // do not create sessions
    }

    /** In-memory user manager based on configuration properties used for basic-authentication. */
    @Bean
    fun inMemoryUserDetailsManager(properties: UsersProperties): InMemoryUserDetailsManager {
        val users = properties.basicAuth.map { user ->
            User.builder()
                .username(user.username)
                .password(passwordEncoder.encode(user.password))
                .authorities(user.authorities.map(::SimpleGrantedAuthority))
                .build()
        }
        return InMemoryUserDetailsManager(users)
    }

}
