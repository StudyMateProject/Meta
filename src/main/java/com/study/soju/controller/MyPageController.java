package com.study.soju.controller;

import com.study.soju.entity.Member;
import com.study.soju.service.MyPageService;
import com.study.soju.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/mypage")
public class MyPageController {
    @Autowired
    SignUpService signUpService;
    @Autowired
    MyPageService myPageService;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 회원정보 수정 페이지
    @GetMapping("/modifyform")
    public String modifyMemberInfo(Model model, Principal principal){
        Member.rpModifyMember rpModifyMember = myPageService.selectMember(principal);
        model.addAttribute("member", rpModifyMember);
        model.addAttribute("memberDTO", new Member.rqModifyMember());
        return "MyPage/ModifyMemberInfo";
    }

    // 회원정보 수정
    @PostMapping("/modifyform/modify")
    public String modifyMember(Member.rqModifyMember rqModifyMember){
        myPageService.modify(rqModifyMember);
        return "redirect:/mypage/modifyform";
    }

    // 비밀번호 변경 페이지
    @GetMapping("/modifyform/editpwd")
    public String findPwdForm(Model model, Principal principal) {
        Member.rpNickname rpNickname = signUpService.memberNickname(principal.getName());
        //바인딩
        model.addAttribute("emailId", principal.getName());
        model.addAttribute("nickname", rpNickname.getNickname());
        return "/MyPage/ResetMyPwd";
    }
}
