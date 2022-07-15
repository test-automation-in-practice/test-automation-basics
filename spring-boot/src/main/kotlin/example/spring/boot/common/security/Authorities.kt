package example.spring.boot.common.security

import example.spring.boot.domains.books.model.Roles.CURATOR
import example.spring.boot.domains.books.model.Roles.USER

object Authorities {

    const val SCOPE_API = "SCOPE_API"
    const val SCOPE_ACTUATOR = "SCOPE_ACTUATOR"

    const val ROLE_USER = "ROLE_$USER"
    const val ROLE_CURATOR = "ROLE_$CURATOR"

}
