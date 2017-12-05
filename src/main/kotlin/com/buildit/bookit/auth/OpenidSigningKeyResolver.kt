package com.buildit.bookit.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.SigningKeyResolverAdapter
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

internal object OpenidSigningKeyResolver : SigningKeyResolverAdapter() {
    private val log = LoggerFactory.getLogger(this::class.java)
    @Suppress("LateinitUsage")
    private lateinit var jwkConfig: JSONObject

    override fun resolveSigningKey(header: JwsHeader<*>, claims: Claims): Key? = loadPublicKey(header.getKeyId())

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
     */
    private fun loadPublicKey(soughtKid: String): PublicKey? {
        if (!::jwkConfig.isInitialized) {
            jwkConfig = fetchJwkConfig()
        }
        val key = jwkConfig.getJSONArray("keys").filterIsInstance(JSONObject::class.java).find { it.getString("kid") == soughtKid }
        return if (key != null) {
            parsePublicKey(key)
        } else {
            jwkConfig = fetchJwkConfig()
            val newKey = jwkConfig.getJSONArray("keys").filterIsInstance(JSONObject::class.java).find { it.getString("kid") == soughtKid }
            newKey?.let { parsePublicKey(it) }
        }
    }

    private fun parsePublicKey(key: JSONObject): PublicKey? {
        val keyStr = makePemCertificate(key)
        /*
         * go to https://jwt.io/ and copy'n'paste the jwt token to the left side, it will be decoded on the right side,
         * copy'n'past the public key (from ----BEGIN... to END CERT...) to the verify signature, it will show signature verified.
         */

        // read certification
        val cer: Certificate?
        val fact = CertificateFactory.getInstance("X.509")
        val stream = ByteArrayInputStream(keyStr.toByteArray(StandardCharsets.US_ASCII))
        cer = fact.generateCertificate(stream)
        if (log.isTraceEnabled) {
            log.trace("AAD OpenID X509Certificate: {}", cer)
        }

        // get public key from certification
        val publicKey = cer.publicKey
        if (log.isDebugEnabled) {
            log.debug("AAD OpenID X509Certificate publicKey: {}", publicKey)
        }
        return publicKey
    }

    private fun fetchJwkConfig(): JSONObject {
        val openidConfigStr = URL("https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration").readText()
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

        return JSONObject(jwkConfigStr)
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
