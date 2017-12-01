package com.buildit.bookit.auth

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Because Spring guys decided a static call is a cool way to go.
 */
open class SecurityContextHolderWrapper {
    fun obtainContext(): SecurityContext = SecurityContextHolder.getContext()
}
