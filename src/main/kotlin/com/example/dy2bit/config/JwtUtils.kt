package com.example.dy2bit.config

import com.example.dy2bit.member.service.UserDetailsServiceImpl
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.http.HttpServletRequest

@Component
class JwtUtils(
    @Value("\${TOKEN_SECRET_KEY:spring-security}")
    private val secretKey: String,
    private val userDetailsServiceImpl: UserDetailsServiceImpl
) {
    private val encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    val EXP_TIME: Long = 1000L * 6000 * 3000

    fun createToken(accountId: String): String =
        Jwts.builder()
            .setSubject(accountId)
            .setExpiration(Date(System.currentTimeMillis() + EXP_TIME))
            .signWith(SignatureAlgorithm.HS384, encodedSecretKey)
            .compact()

    fun getData(token: String): String =
        Jwts.parser()
            .setSigningKey(encodedSecretKey)
            .parseClaimsJws(token)
            .body
            .subject

    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsServiceImpl.loadUserByUsername(getData(token))
        return UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities,
        )
    }

    fun extractToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")

    fun validateToken(token: String) =
        try {
            val expirationTime = Jwts.parser()
                .setSigningKey(encodedSecretKey)
                .parseClaimsJws(token)
                .body
                .expiration
            expirationTime.after(Date())
        } catch (e: Exception) { false }
}
