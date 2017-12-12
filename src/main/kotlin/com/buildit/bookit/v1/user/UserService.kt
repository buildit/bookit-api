package com.buildit.bookit.v1.user

import com.buildit.bookit.auth.UserPrincipal
import com.buildit.bookit.v1.booking.dto.User
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {
    /**
     * Obtains the Bookit user representa
     */
    fun register(principal: UserPrincipal): User = try {
        userRepository.getUserByExternalId(principal.subject)
    } catch (e: EmptyResultDataAccessException) {
        userRepository.insertUser(principal.subject, principal.givenName, principal.familyName)
    }
}
