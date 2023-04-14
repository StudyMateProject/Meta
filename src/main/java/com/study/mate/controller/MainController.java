package com.study.mate.controller;

import com.study.mate.entity.Alarm;
import com.study.mate.entity.Member;
import com.study.mate.entity.RecruitMentee;
import com.study.mate.entity.RecruitStudy;
import com.study.mate.service.MyPageService;
import com.study.mate.service.RecruitMenteeService;
import com.study.mate.service.RecruitStudyService;
import com.study.mate.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class MainController {
    @Autowired
    SignUpService signUpService;
    @Autowired
    MyPageService myPageService;
    @Autowired
    RecruitStudyService recruitStudyService;
    @Autowired
    RecruitMenteeService recruitMenteeService;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 로그인 후 메인 페이지
    @GetMapping("/")
    public String main(Principal principal, Model model) {
        // 1. 로그인을 했는지 체크한다.
        // 1-1. 로그인을 안한 경우
        if ( principal == null ) {
            // 1-1-1. 로그인 전 메인 페이지로 리다이렉트한다.
            return "redirect:/n";
        // 1-2. 로그인을 한 경우
        } else {
            // 1-2-1. 1에서 파라미터로 받아온 로그인 유저 아이디를 서비스에 전달한다.
            Member.rpNickname rpNickname = signUpService.memberNickname(principal.getName());
            // 본인에게 온 알람 리스트 검색
            List<Alarm> alarmList = myPageService.findEmailId(principal.getName());
            // 가장 인기있는 스터디 리스트 검색
            List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(1, 10);
            // 요즘 주목받는 멘토 리스트 검색
            List<RecruitMentee> recruitMenteeList = recruitMenteeService.recruitMenteeListAll(1, 10);

            // 1-2-2. 1-2-1에서 받아온 로그인 유저 닉네임을 바인딩한다.
            model.addAttribute("nickname", rpNickname.getNickname());
            model.addAttribute("alarmList", alarmList);
            model.addAttribute("list", recruitStudyList);
            model.addAttribute("menteeList", recruitMenteeList);
            // 1-2-3. 메인 페이지로 이동한다.
            return "Main";
        }
    }

    // 로그인 전 메인 페이지
    @GetMapping("/n")
    public String nmain(Model model) {
        // 가장 인기있는 스터디 리스트 검색
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(1, 10);
        // 요즘 주목받는 멘토 리스트 검색
        List<RecruitMentee> recruitMenteeList = recruitMenteeService.recruitMenteeListAll(1, 10);

        model.addAttribute("list", recruitStudyList);
        model.addAttribute("menteeList", recruitMenteeList);
        // 1. 메인 페이지로 이동한다.
        return "Main";
    }

    // 로그인 페이지
    @GetMapping("/loginform") // required = false - 해당 필드가 URL 파라미터에 존재하지 않아도 에러가 발생하지 않는다.
    public String loginform(@RequestParam(value = "error", required = false) String error, // 1-1. URL 파라미터를 통해 넘어오는 에러 체크값이 있을 경우 받는다.
                            @RequestParam(value = "errorMsg", required = false) String errorMsg, // 1-2. URL 파라미터를 통해 넘어오는 에러 메세지가 있을 경우 받는다.
                            @RequestParam(value = "loginErrMsg", required = false) String loginErrMsg, // 1-3. URL 파라미터를 통해 넘어오는 소셜 로그인 에러 메세지가 있을 경우 받는다.
                            Model model) { // 1. 파라미터로 넘어오는 각종 에러값들을 받아온다.
        // 구글 로그인 API를 사용하기 위해선 몇가지 과정을 거쳐야 한다.
        // 2. 가장 먼저 code가 필요하기에 여기서 code를 받아오는 구글 서버 URL을 미리 만들어서 가져간다.
        String googleUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                           "scope=" + "email profile https://www.googleapis.com/auth/user.birthday.read https://www.googleapis.com/auth/user.gender.read https://www.googleapis.com/auth/user.phonenumbers.read" +
                           "&access_type=" + "offline" +
                           "&include_granted_scopes=" + "true" +
                           "&response_type=" + "code" +
                           "&state=" + "security_token%3D138r5719ru3e1%26url%3Dhttps://oauth2.example.com/token" +
                           "&redirect_uri=" + "http://localhost:8888/loginform/googletoken" +
                           "&client_id=" + "346535144521-qthfl467cl8jrdcdc2g8l3lvpurqsk1h.apps.googleusercontent.com";
        // 3. 구글 로그인 URL을 바인딩한다.
        model.addAttribute("googleUrl", googleUrl);
        // 4. 1-1에서 받아온 에러 체크값을 바인딩한다.
        model.addAttribute("error", error);
        // 5. 1-2에서 받아온 에러 메세지를 바인딩한다.
        model.addAttribute("errorMsg", errorMsg);
        // 6. 1-3에서 받아온 소셜 로그인 에러 메세지를 바인딩한다.
        model.addAttribute("loginErrMsg", loginErrMsg);
        // 7. 로그인 페이지로 이동한다.
        return "SignUp/LoginForm";
    }

    // 로그아웃 페이지
    @PostMapping("/logout")
    public void logout() {
        // 로그아웃 후 이동 페이지는 Spring Security가 각 핸들러를 통해 잡고 있기에 여기서 굳이 잡아줄 필요가 없다.
        // 메인 페이지로 이동한다.
        // return "Main";
    }
}