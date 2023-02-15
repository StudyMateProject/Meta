package com.study.soju.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class MainController {
    // 로그인 후 메인 페이지
    @GetMapping("/")
    public String main(Principal principal) {
        if ( principal == null ) {
            return "redirect:/n";
        }
        // 메인 페이지로 이동
        return "Main";
    }

    // 로그인 전 메인 페이지
    @GetMapping("/n")
    public String nmain() {
        // 메인 페이지로 이동
        return "Main";
    }

    // 로그인 페이지
    @GetMapping("/loginform") // required = false - 해당 필드가 URL 파라미터에 존재하지 않아도 에러가 발생하지 않는다.
    public String loginform(@RequestParam(value = "error", required = false) String error, // 1-1. URL 파라미터로 넘어오는 에러 체크값이 있을 경우 받는다.
                            @RequestParam(value = "errorMsg", required = false) String errorMsg, // 1-2. URL 파라미터로 넘어오는 에러 메세지가 있을 경우 받는다.
                            Model model) { // 1. 파라미터로 넘어오는 각종 에러값들을 받아온다.
        // 2. 1-1에서 받아온 에러 체크값을 바인딩 한다.
        model.addAttribute("error", error);
        // 3. 1-2에서 받아온 에러 메세지를 바인딩 한다.
        model.addAttribute("errorMsg", errorMsg);
        // 로그인 페이지로 이동
        return "SignUp/LoginForm";
    }

    // 로그아웃 페이지
    @PostMapping("/logout")
    public void logout() {
        // 로그아웃 후 이동 페이지는 Spring Security가 각 핸들러를 통해 잡고 있기에 여기서 굳이 잡아줄 필요가 없다.
        // 메인 페이지로 이동
        // return "Main";
    }
}
