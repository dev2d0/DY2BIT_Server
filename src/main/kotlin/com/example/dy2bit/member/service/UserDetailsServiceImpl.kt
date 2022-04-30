package com.example.dy2bit.member.service

import com.example.dy2bit.repository.MemberRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val memberRepository: MemberRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return memberRepository.findByEmail(username) ?: throw UsernameNotFoundException("존재하지 않는 유저 입니다.")
    }
}
