package com.buildit.bookit.v1.user

import com.buildit.bookit.auth.UserPrincipal
import com.buildit.bookit.v1.booking.dto.User
import org.springframework.stereotype.Service

@Service
class UserService {
    fun register(principal: UserPrincipal): User =
        User(principal.subject, "${principal.givenName} ${principal.familyName}")
}
