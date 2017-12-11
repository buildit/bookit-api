package com.buildit.bookit.auth

import com.buildit.bookit.v1.booking.dto.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class WithMockCustomUser(
    val subject: String = "123abc",
    val givenName: String = "Fake",
    val familyName: String = "User"
)

@Suppress("UnsafeCast")
fun makeUser(kClass: KClass<out Any>): User =
    kClass.annotations.find { it.annotationClass == WithMockCustomUser::class }.let {
        val ann = it as WithMockCustomUser
        return User(ann.subject, "${ann.givenName} ${ann.familyName}")
    }

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
    override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()

        val principal = UserPrincipal(customUser.subject, customUser.familyName, customUser.givenName)
        val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        context.authentication = auth
        return context
    }
}
