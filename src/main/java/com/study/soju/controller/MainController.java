package com.study.soju.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    // 메인 페이지
    @GetMapping(value = "/")
    public String main() {
        return "Main";
    }

    // 로그인 페이지
    @GetMapping("/loginform") // required = false - 해당 필드가 URL 파라미터에 존재하지 않아도 에러가 발생하지 않는다.
    public String loginform(@RequestParam(value = "error", required = false) String error, // URL 파라미터로 넘어온 에러 체크값이 있을 경우 받는다.
                            @RequestParam(value = "errorMsg", required = false) String errorMsg, // URL 파라미터로 넘어온 에러 메세지가 있을 경우 받는다.
                            Model model) {
        // 에러 체크값을 모델로 바인딩 한다.
        model.addAttribute("error", error);
        // 에러 메세지를 모델로 바인딩 한다.
        model.addAttribute("errorMsg", errorMsg);
        return "SignUp/LoginForm";
    }
}
