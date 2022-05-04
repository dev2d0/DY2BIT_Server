package com.example.dy2bit.member.service

import com.example.dy2bit.config.JwtUtils
import com.example.dy2bit.member.model.dto.UserLoginDTO
import com.example.dy2bit.model.Member
import com.example.dy2bit.repository.MemberRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val jwtUtils: JwtUtils
) {
    @Transactional(readOnly = true)
    fun findUser(email: String): Member {
        return memberRepository.findByEmail(email) ?: throw UsernameNotFoundException("존재하지 않는 유저 입니다.")
    }

    @Transactional(readOnly = true)
    fun existsUser(email: String): Boolean {
        return memberRepository.existsByEmail(email)
    }

    @Transactional(readOnly = true)
    fun isLoginSuccess(email: String, password: String): Boolean {
        val user = findUser(email)
        if (password != user.password) {
            return false
        }
        return true
    }

    fun logIn(email: String, password: String): UserLoginDTO {
        val token: String = jwtUtils.createToken(email)

        return UserLoginDTO(HttpStatus.OK, token)
    }
}
