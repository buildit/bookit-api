package com.buildit.bookit.v1.user

import com.buildit.bookit.auth.UserPrincipal
import com.buildit.bookit.v1.booking.dto.User
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {
    /**
     * Obtains the Bookit user representa
     */
    fun register(principal: UserPrincipal): User {
        val knownUser = userRepository.getUserByExternalId(principal.subject)
        if (knownUser != null) return knownUser

        return userRepository.insertUser(principal.subject, principal.givenName, principal.familyName)
    }
}
