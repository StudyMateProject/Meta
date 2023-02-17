package com.study.soju.controller;

import com.study.soju.entity.Member;
import com.study.soju.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignUpController {
    // 회원가입 및 로그인 인증 서비스
    @Autowired
    SignUpService signUpService;
    // 비밀번호 암호화 메소드
    @Autowired
    PasswordEncoder passwordEncoder;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 로그인 진행 URL
    @PostMapping("/loginform/login")
    public void login(@RequestParam(value = "emailId") String emailId) { // 1. 파라미터로 로그인 할때 작성한 아이디를 받아온다.
        // 2. 1에서 받아온 아이디를 서비스에 전달하다.
        signUpService.loadUserByUsername(emailId);
        // 로그인 성공 및 실패 후 이동 페이지는 Spring Security가 각 핸들러를 통해 잡고 있기에 여기서 굳이 잡아줄 필요가 없다.
        // 메인 페이지로 이동한다.
        // return "Main";
    }

    // 회원가입 페이지
    @GetMapping("/joinform")
    public String joinform(Model model) {
        // 1. 회원가입에 사용할 DTO를 바인딩한다.
        model.addAttribute("memberDTO", new Member.rqJoinMember());
        // 2. 회원가입 페이지로 이동한다.
        return "SignUp/JoinForm";
    }

    // 회원가입 진행 URL
    @PostMapping("/joinform/join")
    public String join(Member.rqJoinMember rqJoinMember, Model model) { // 1. 파라미터로 form을 통해 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 넘어온 DTO와 비밀번호 암호화 메소드를 같이 서비스에 전달한다.
        Member.rpJoinMember member = signUpService.joinMember(rqJoinMember, passwordEncoder);
        // 11. 2에서 반환받은 DTO를 바인딩한다.
        model.addAttribute("member", member);
        // 12. 환영 페이지로 이동한다.
        return "SignUp/Welcome";
    }
}
