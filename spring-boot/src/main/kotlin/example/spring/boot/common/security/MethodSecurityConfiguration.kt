package example.spring.boot.common.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

/**
 * Enable general method-level security annotations like `@RolesAllowed`.
 *
 * In a separate configuration class in order to be able to selectively activate this in tests.
 */
@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
class MethodSecurityConfiguration
