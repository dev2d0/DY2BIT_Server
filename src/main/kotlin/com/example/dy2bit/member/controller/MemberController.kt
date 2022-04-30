package com.example.dy2bit.member.controller

import com.example.dy2bit.member.service.MemberService
import com.example.dy2bit.member.model.dto.UserLoginDTO
import com.example.dy2bit.member.model.dto.UserLoginForm
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*

@RestController
class MemberController(
    private val memberService: MemberService
) {
    @CrossOrigin(origins = arrayOf("*"))
    @PostMapping("/login")
    fun logIn(@RequestBody userLoginReq: UserLoginForm): ResponseEntity<UserLoginDTO> {
        if (!memberService.existsUser(userLoginReq.email)) {
            throw UsernameNotFoundException("존재하지 않는 유저 입니다.")
        }

        if (!memberService.isLoginSuccess(userLoginReq.email, userLoginReq.password)) {
            throw UsernameNotFoundException("일치 하지 않는 비밀번호 입니다.")
        }

        return ResponseEntity.ok(memberService.logIn(userLoginReq.email, userLoginReq.password))
    }
}
