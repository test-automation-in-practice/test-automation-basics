package example.spring.boot.common.security

import example.spring.boot.domains.books.model.Roles.CURATOR
import example.spring.boot.domains.books.model.Roles.USER

/**
 * List of possible security authorizations users can have.
 */
object Authorities {

    /** General access to the API. */
    const val SCOPE_API = "SCOPE_API"

    /** Access to actuator features. */
    const val SCOPE_ACTUATOR = "SCOPE_ACTUATOR"

    /** A general application user. */
    const val ROLE_USER = "ROLE_$USER"

    /** A special user with some administrative rights. */
    const val ROLE_CURATOR = "ROLE_$CURATOR"

}
