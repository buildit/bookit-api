package com.buildit.bookit.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SigningKeyResolverAdapter
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.PublicKey
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.ArrayList
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// TODO: kotlinify it
// TODO: check log levels
class JwtAuthenticationFilter(authManager: AuthenticationManager) : BasicAuthenticationFilter(authManager) {
    private val TOKEN_PREFIX = "Bearer "
    private val HEADER_STRING = "Authorization"

    override fun doFilterInternal(req: HttpServletRequest,
                                  res: HttpServletResponse,
                                  chain: FilterChain) {
        val header = req.getHeader(HEADER_STRING)

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res)
            return
        }

        val authentication = getAuthentication(req)

        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(req, res)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(HEADER_STRING)
        if (token != null) {
            // parse the token.
            var user: String? = null
            try {
                user = Jwts.parser()
                    .setSigningKeyResolver(OpenidSigningKeyResolver())
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .body
                    .subject
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (user != null) {
                log.info("Request token verification success. {}", user)
                return UsernamePasswordAuthenticationToken(user, null, ArrayList<GrantedAuthority>())
            }
            log.info("Request token verification failure. {}", user)

            return null
        }
        return null
    }


    private inner class OpenidSigningKeyResolver : SigningKeyResolverAdapter() {
        override fun resolveSigningKey(header: JwsHeader<*>, claims: Claims): Key? {
            return loadPublicKey(header.getKeyId())
        }

        /**
         * 1. go to here: https://login.microsoftonline.com/common/.well-known/openid-configuration
         * 2. check the value of "jwks_uri", which is "https://login.microsoftonline.com/common/discovery/keys"
         * 3. go to https://login.microsoftonline.com/common/discovery/keys
         * 4. get "kid" value from header, which is "Y4ueK2oaINQiQb5YEBSYVyDcpAU"
         * 5. search Y4ueK2oaINQiQb5YEBSYVyDcpAU in key file to get the key.
         *
         * (We can manually decode JWT token at https://jwt.io/ by copy'n'paste)
         * to select the public key used to sign this token.
         * (There are about three keys which are rotated about everyday.)
         *
         * @throws IOException
         * @throws CertificateException
         */
        private fun loadPublicKey(soughtKid: String): PublicKey? {
            //TODO: cache content to file to prevent access internet everytime.

            // Key Info (RSA PublicKey)
            val openidConfigStr = URL("https://login.microsoftonline.com/common/.well-known/openid-configuration").readText()
            if (log.isDebugEnabled) {
                log.debug("AAD OpenID Config: {}", openidConfigStr)
            }

            val openidConfig = JSONObject(openidConfigStr)
            val jwksUri = openidConfig.getString("jwks_uri")
            if (log.isDebugEnabled) {
                log.debug("AAD OpenID Config jwksUri: {}", jwksUri)
            }

            val jwkConfigStr = URL(jwksUri).readText()
            if (log.isDebugEnabled) {
                log.debug("AAD OpenID JWK Config: {}", jwkConfigStr)
            }

            val jwkConfig = JSONObject(jwkConfigStr)
            val keys = jwkConfig.getJSONArray("keys")
            for (i in 0 until keys.length()) {
                val key = keys.getJSONObject(i)

                val kid = key.getString("kid")
                if (soughtKid != kid) {
                    continue
                }

                val keyStr = makePemCertificate(key)

                /*
             * go to https://jwt.io/ and copy'n'paste the jwt token to the left side, it will be decoded on the right side,
             * copy'n'past the public key (from ----BEGIN... to END CERT...) to the verify signature, it will show signature verified.
             */

                // read certification
                var cer: X509Certificate? = null
                try {
                    val fact = CertificateFactory.getInstance("X.509")
                    val stream = ByteArrayInputStream(keyStr.toByteArray(StandardCharsets.US_ASCII))
                    cer = fact.generateCertificate(stream) as X509Certificate
                    if (log.isTraceEnabled) {
                        log.trace("AAD OpenID X509Certificate: {}", cer)
                    }
                } catch (e: CertificateException) {
                    throw RuntimeException(e)
                }

                // get public key from certification
                val publicKey = cer.publicKey
                if (log.isDebugEnabled) {
                    log.debug("AAD OpenID X509Certificate publicKey: {}", publicKey)
                }

                return publicKey
            }
            return null
        }

        private fun makePemCertificate(key: JSONObject): String {
            val x5c = key.getJSONArray("x5c").getString(0)
            val certParts = x5c.split("(?<=\\G.{64})".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var keyStr = "-----BEGIN CERTIFICATE-----\r\n"
            keyStr += certParts.joinToString("\r\n")
            keyStr += "-----END CERTIFICATE-----\r\n"

            if (log.isDebugEnabled) {
                log.debug("AAD OpenID Key:\n{}", keyStr)
            }
            return keyStr
        }

    }

    companion object {
        private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
}
