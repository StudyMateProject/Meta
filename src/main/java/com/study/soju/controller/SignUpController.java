package com.study.soju.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.study.soju.entity.Member;
import com.study.soju.httpclient.GoogleLogin;
import com.study.soju.httpclient.IamPortPass;
import com.study.soju.service.SignUpOAuthService;
import com.study.soju.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

@PropertySource("classpath:application-information.properties")
@Controller
public class SignUpController {
    // 회원가입 및 로그인 인증 서비스
    @Autowired
    SignUpService signUpService;
    // 소셜 로그인 인증 서비스
    @Autowired
    SignUpOAuthService signUpOAuthService;
    // 비밀번호 암호화 메소드
    @Autowired
    PasswordEncoder passwordEncoder;

    // properties - SMTP ID/PWD
    @Value("${naverId:naverId}")
    private String naverId;
    @Value("${naverPwd:naverPwd}")
    private String naverPwd;
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

    // 이메일 중복체크 & 이메일 전송 SMTP
    @PostMapping("/joinform/emailsend")
    @ResponseBody
    public Member.rpCheckEmailId emailSend(String emailId) { // 2. 파라미터로 Ajax를 통해 넘어온 아이디를 받아온다.
        // 3. 2에서 파라미터로 받아온 아이디와 비밀번호 암호화 메소드를 서비스에 전달한다.
        //    이때 SMTP에 사용할 이메일 정보들도 같이 서비스에 전달한다.
        Member.rpCheckEmailId rpCheckEmailId =  signUpService.checkEmailId(emailId, passwordEncoder, naverId, naverPwd);
        // 13. 3에서 반환된 DTO를 콜백 메소드에 반환한다.
        return rpCheckEmailId;
    }

    // 이메일 인증 번호 체크
    @PostMapping("/joinform/emailsend/emailcheck")
    @ResponseBody
    public boolean emailCheck(String emailKey, String hEmailKey) { // 1. 파라미터로 fetch를 통해 넘어온 작성한 이메일 인증 번호와 이메일 인증 번호 체크 값(암호화된 이메일 인증 번호)을 받아온다.
        // 2. passwordEncoder.matches()를 사용하여 암호화된 이메일 인증 번호와 작성한 이메일 인증 번호가 일치하는지 체크한다.
        //    passwordEncoder.matches() - Spring Security에서 제공하는 메소드 중 하나로,
        //                                첫 번째 매개변수에는 사용자가 입력한 문자열을, 두 번째 매개변수에는 인코딩된 문자열을 전달하여,
        //                                입력된 문자열과 인코딩된 문자열을 비교하여 일치하는지 확인하고,
        //                                boolean(true/false) 타입으로 결과 값을 반환한다.
        //                                passwordEncoder.encode() 메소드를 사용하여 입력된 문자열을 인코딩한 후,
        //                                이 인코딩된 문자열과 사용자가 입력한 문자열을 비교하고자 할 때 passwordEncoder.matches()를 사용할 수 있다.
        boolean isMatch = passwordEncoder.matches(emailKey, hEmailKey);
        // 3. 2에서 반환된 결과 값(true/false)을 클라이언트로 반환한다.
        //    true - 이메일 인증 번호 일치 / false - 이메일 인증 번호 불일치
        return isMatch;
    }

    // 닉네임 중복체크
    @PostMapping("/joinform/nicknamecheck")
    @ResponseBody
    public String nicknameCheck(String nickname) {
        String checkNickname = signUpService.checkNickname(nickname); //닉네임 존재 여부
        return checkNickname;
    }

    // 휴대폰 본인인증 IamPort서버 통신
    @PostMapping("/joinform/certifications")
    @ResponseBody
    public HashMap<String, String> certifications(String impUid) { // 4. 파라미터로 Ajax를 통해 넘어온 imp_uid를 받아온다.
        // 5. IamPort로부터 access_token을 받아와야 하기에 IamPort 서버와 통신하는 메소드를 실행한다.
        JsonNode jsonToken = IamPortPass.getToken();
        // 5에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        //System.out.println(jsonToken);
        // 17. 5에서 반환받은 값에서 필요한 access_token을 가져와 토큰 변수에 전달한다.
        // toString - 객체나 값을 문자열로 변환하는 메소드로, 디버깅이나 출력용으로 사용한다. - "" 큰 따옴표가 항시 따라 붙는다.
        // asText - 일반적으로 사람이 읽을 수 있는 형식으로 값을 문자열로 변환하는 데 사용한다. - "" 큰 따옴표 없이 글자만 나온다.
        // 여기서 두가지의 눈에 띄는 차이는 "" 큰 따옴표의 유무인데, 값을 URL에 담아서 함께 보내는 GET 방식에는 데이터에 따라 붙는 이 "" 큰 따옴표가 걸리적 거리기에 반드시 asText로 변환해야 한다.
        String accessToken = jsonToken.get("response").get("access_token").asText();

        // 18. 4에서 파라미터로 받아온 imp_uid와 17에서 전달받은 access_token으로 이번엔 IamPort를 통해 인증된 유저 정보를 받아와야 하기에 IamPort 서버와 통신하는 메소드에 imp_uid와 access_token을 전달한다.
        JsonNode userInfo = IamPortPass.getUserInfo(impUid, accessToken);
        // 18에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        //System.out.println(userInfo);
        // 29. 18에서 반환받은 값에서 필요한 유저 정보인 이름과 생년월일과 휴대폰 번호를 가져온다.
        // 29-1. 29에서 가져온 이름을 이름 변수에 전달한다.
        String name = userInfo.get("response").get("name").asText();
        // 29-2. 29에서 가져온 생년월일을 생년월일 변수에 전달한다.
        String birthday = userInfo.get("response").get("birthday").asText();
        // 29-3. 29에서 가져온 휴대폰 번호를 휴대폰 번호 변수에 전달한다.
        String phoneNumber = userInfo.get("response").get("phone").asText();

        // 30. 29에서 가져온 유저 정보들을 모두 한번에 콜백 메소드로 반환하기 위해 Map을 생성한다.
        HashMap<String, String> map = new HashMap<>(); //생년월일 / 이름 / 핸드폰 번호 HashMap으로 만들어 전송
        // 30-1. 30에서 생성한 Map에 29-1, 29-2, 29-3에서 각각 나눠 전달받은 유저 정보들을 name/value 쌍으로 담아준다.
        map.put("name", name);
        map.put("birthday", birthday);
        map.put("phoneNumber", phoneNumber);
        // 31. 30에서 만든 유저 정보 Map을 콜백 메소드에 반환한다.
        return map;
    }

    // 휴대폰 번호로 중복 가입자 체크
    @PostMapping("/joinform/checkphone")
    @ResponseBody
    public String checkPhone(String phoneNumber) { // 36. 파라미터로 Ajax를 통해 넘어온 휴대폰 번호를 받아온다.
        // 37. 36에서 파라미터로 받아온 휴대폰 번호를 서비스에 전달한다.
        String checkPhone = signUpService.checkPhone(phoneNumber); //핸드폰 번호 존재 여부
        // 41. 37에서 반환된 값을 콜백 메소드에 반환한다.
        return checkPhone;
    }

    // 회원가입 진행 URL
    @PostMapping("/joinform/join")
    @ResponseBody
    public String join(Member.rqJoinMember rqJoinMember, Model model) { // 1. 파라미터로 form을 통해 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 넘어온 DTO와 비밀번호 암호화 메소드를 서비스에 전달한다.
        String res = signUpService.joinMember(rqJoinMember, passwordEncoder);
        // 7. 2에서 반환받은 회원가입 결과 값을 클라이언트로 반환한다.
        return res;
    }
    /////////////////////////////////////////////////// 소셜 로그인 API ///////////////////////////////////////////////////
    // 구글 로그인 토큰 발급 및 유저 정보 조회
    @GetMapping("/loginform/googletoken")
    public String googleAuthentication(String code, Model model) { // 4. 파라미터로 3에서 구글 로그인 URL을 통해 가져온 code를 받아온다.
        // 5. 4에서 파라미터로 받아온 code로 이번엔 access_token을 받아와야 하기에 구글 서버와 통신하는 메소드에 code를 전달한다.
        JsonNode jsonToken = GoogleLogin.getAccessToken(code);
        // 5에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        //System.out.println(jsonToken);
        // 17. 5에서 반환받은 값에서 필요한 access_token을 가져와 토큰 변수에 전달한다.
        // toString - 객체나 값을 문자열로 변환하는 메소드로, 디버깅이나 출력용으로 사용한다. - "" 큰 따옴표가 항시 따라 붙는다.
        // asText - 일반적으로 사람이 읽을 수 있는 형식으로 값을 문자열로 변환하는 데 사용한다. - "" 큰 따옴표 없이 글자만 나온다.
        // 여기서 두가지의 눈에 띄는 차이는 "" 큰 따옴표의 유무인데, 값을 URL에 담아서 함께 보내는 GET 방식에는 데이터에 따라 붙는 이 "" 큰 따옴표가 걸리적 거리기에 반드시 asText를 사용해야 한다.
        String accessToken = jsonToken.get("access_token").asText();

        // 18. 17에서 전달받은 access_token으로 이번엔 구글 로그인 유저 정보를 받아와야 하기에 구글 서버와 통신하는 메소드에 access_token을 전달한다.
        JsonNode userInfo = GoogleLogin.getGoogleUserInfo(accessToken);
        // 18에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        //System.out.println(userInfo);
        // 29. 18에서 반환받은 값에서 필요한 유저 정보인 이메일과 이름을 가져온다.
        // 29-1. 29에서 가져온 이메일을 아이디 변수에 전달한다.
        String emailId = userInfo.get("email").asText();
        // 29-2. 29에서 가져온 이름을 이름 변수에 전달한다.
        String name = userInfo.get("name").asText();

        // 30. 17에서 전달받은 access_token으로 이번엔 구글 로그인 유저의 추가 정보를 받아와야 하기에 구글 서버와 통신하는 메소드에 access_token을 전달한다.
        JsonNode people = GoogleLogin.getGooglePeople(accessToken);
        // 30에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        //System.out.println(userInfo);
        // 40. 30에서 반환받은 값에서 필요한 추가 유저 정보인 생년월일과 성별을 가져온다.
        // 40-1. 40에서 가져온 생년월일이 년, 월, 일로 따로따로 분리되서 나오기에 먼저 각각 변수로 받은뒤 그 다음 하나로 합쳐서 생일 변수에 전달한다.
        String year = people.get("birthdays").get(0).get("date").get("year").asText();
        String month = people.get("birthdays").get(0).get("date").get("month").asText();
        if ( month.length() < 2 ) {
            month = "0" + month;
        }
        String day = people.get("birthdays").get(0).get("date").get("day").asText();
        if ( day.length() < 2 ) {
            day = "0" + day;
        }
        String birthday = year + "-" + month + "-" + day;
        // 40-2. 40에서 가져온 성별이 male, female로 나오기에 if문을 통해서 DB 규칙에 맞게 각각 M과 F로 만들어서 다시 성별 변수에 전달한다.
        String gender = people.get("genders").get(0).get("value").asText();
        // 40-2-1. gender가 male일 경우
        if ( gender.equals("male") ) {
            // 40-2-1-1. 성별 변수에 M을 전달한다.
            gender = "M";
        // 40-2-2. gender가 female일 경우
        } else {
            // 40-2-2-1. 성별 변수에 F를 전달한다.
            gender = "F";
        }

        // 41. 29-1에서 전달받은 아이디를 서비스에 전달한다.
        Member.rpJoinSocialMember rpJoinSocialMember = signUpOAuthService.findByJoinGoogleMember(emailId);
        // 47. 41에서 반환받은 DTO가 있는지 체크한다.
        // 47-1. 반환받은 DTO가 없는 경우 - 미가입자로 여기서 받아온 구글 유저 정보들을 들고 구글 회원가입 추가입력 페이지로 이동한다.
        if ( rpJoinSocialMember == null ) { // 회원가입
            // 47-1-1. 29-1에서 전달받은 아이디를 바인딩한다.
            model.addAttribute("emailId", emailId);
            // 47-1-2. 29-2에서 전달받은 이름을 바인딩한다.
            model.addAttribute("name", name);
            // 47-1-3. 40-1에서 전달받은 생년월일을 바인딩한다.
            model.addAttribute("birthday", birthday);
            // 47-1-4. 40-2에서 전달받은 성별을 바인딩한다.
            model.addAttribute("gender", gender);
            // 47-1-5. 구글 회원가입에 사용할 DTO를 바인딩한다.
            model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
            // 47-1-6. 구글 회원가입 추가입력 페이지로 이동한다.
            return "SignUp/GoogleJoinForm";
        // 47-2. 반환받은 DTO가 있는 경우 - 구글 가입자 or 타 플랫폼 가입자
        } else {
            // 48. 받환받은 DTO 값 중 Idx를 체크한다.
            // 48-1. idx가 0일 경우 - 구글 이메일을 사용하여 다른 플랫폼으로 가입한 유저
            if ( rpJoinSocialMember.getIdx() == 0 ) { // 에러
                try {
                    // 48-1-1. 에러 메시지를 UTF-8 형식으로 인코딩하여 로그인 페이지로 리다이렉트한다.
                    return "redirect:/loginform?loginErrMsg=" + URLEncoder.encode(rpJoinSocialMember.getErrMsg(), "UTF-8");
                } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
                    throw new RuntimeException(e);
                }
            // 48-2. idx가 0이 아닐 경우 - 구글로 가입한 유저
            } else { // 로그인
                // 48-2-1. Spring Security가 관리하고 있는 OAuth2를 통해 OAuth2UserService로 리다이렉트한다.
                return "redirect:/oauth2/authorization/google";
            }
        }
    }

    // 구글 회원가입 URL
    @PostMapping("/loginform/googlejoin")
    @ResponseBody
    public String googleJoin(Member.rqJoinSocialMember rqJoinSocialMember) { // 1. 파라미터로 form을 통해 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO를 서비스에 전달한다.
        String res = signUpOAuthService.socialJoin(rqJoinSocialMember);
        // 7. 2에서 반환받은 회원가입 결과 값을 클라이언트로 반환한다.
        return res;
    }

    // 네이버 콜백 페이지
    @GetMapping("/loginform/navercallback")
    public String naverCallback(Model model) {
        // 1. 네이버 유저 정보를 가져올때 사용할 DTO를 바인딩한다.
        model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
        // 2. 네이버 콜백 페이지로 이동한다.
        return "SignUp/NaverCallback";
    }

    // 네이버 로그인 인증
    @PostMapping("/loginform/naverauthentication")
    @ResponseBody
    public String naverAuthentication(Member.rqJoinSocialMember rqJoinSocialMember) { // 1. 파라미터로 네이버 콜백 페에지에서 Ajax를 통해 넘어온 값들을 DTO로 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO를 서비스에 전달한다.
        Member.rpJoinSocialMember rpJoinSocialMember = signUpOAuthService.findByJoinNaverMember(rqJoinSocialMember);
        // 9. 반환받은 DTO가 있는지 체크한다.
        // 9-1. 반환받은 DTO가 없는 경우 - 미가입자
        if ( rpJoinSocialMember == null ) { // 회원가입
            // 9-1-1. "0"을 반환한다. - 콜백 메소드에서 다음 네이버 회원가입 페이지로 이동시킨다.
            return "0";
        // 9-2. 반환받은 DTO가 있는 경우 - 네이버 가입자 or 타 플랫폼 가입자
        } else {
            // 10. 받환받은 DTO 값 중 Idx를 체크한다.
            // 10-1. idx가 0일 경우 - 네이버 이메일을 사용하여 다른 플랫폼으로 가입한 유저
            if ( rpJoinSocialMember.getIdx() == 0 ) { // 에러
                // 10-1-1. 에러메세지를 반환한다. - 콜백 메소드에서 에러메세지를 띄운뒤 로그인 페이지로 이동시킨다.
                return rpJoinSocialMember.getErrMsg();
            // 10-2. idx가 0이 아닐 경우 - 네이버로 가입한 유저
            } else { // 로그인
                // 10-2-1. "1"을 반환한다. - 콜백 메소드에서 Spring Security가 관리하고 있는 OAuth2를 통해 OAuth2UserService로 이동시킨다.
                return "1";
            }
        }
    }

    // 네이버 회원가입 추가입력 페이지
    @PostMapping("/loginform/naverjoinform")
    public String naverJoinForm(Member.rqJoinSocialMember rqJoinSocialMember, Model model) { // 1. 파라미터로 form을 통해 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO에 들어있는 값들을 하나씩 꺼내서 각각 바인딩한다.
        // 2-1. 1에서 파라미터로 받아온 DTO 값 중 아이디를 바인딩한다.
        model.addAttribute("emailId", rqJoinSocialMember.getEmailId());
        // 2-2. 1에서 파라미터로 받아온 DTO 값 중 이름을 바인딩한다.
        model.addAttribute("name", rqJoinSocialMember.getName());
        // 2-3. 1에서 파라미터로 받아온 DTO 값 중 휴대폰 번호를 바인딩한다.
        model.addAttribute("phoneNumber", rqJoinSocialMember.getPhoneNumber());
        // 2-4. 1에서 파라미터로 받아온 DTO 값 중 성별을 바인딩한다.
        model.addAttribute("gender", rqJoinSocialMember.getGender());
        // 2-5. 1에서 파라미터로 받아온 DTO 값 중 생일을 바인딩한다.
        model.addAttribute("birthday", rqJoinSocialMember.getBirthday());
        // 2-6. 네이버 회원가입에 사용할 DTO를 바인딩한다.
        model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
        // 2-7. 네이버 회원가입 추가입력 페이지로 이동한다.
        return "SignUp/NaverJoinForm";
    }

    // 네이버 회원가입 URL
    @PostMapping("/loginform/naverjoin")
    @ResponseBody
    public String naverJoin(Member.rqJoinSocialMember rqJoinSocialMember) { // 1. 파라미터로 form을 통해 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO를 서비스에 전달한다.
        String res = signUpOAuthService.socialJoin(rqJoinSocialMember);
        // 7. 2에서 반환받은 회원가입 결과 값을 클라이언트로 반환한다.
        return res;
    }
    ////////////////////////////////////////////////////// ID 찾기 //////////////////////////////////////////////////////
    //ID 찾기 페이지 이동
    @GetMapping("/loginform/findidform")
    public String findIdForm(Model model) {
        //바인딩
        model.addAttribute("memberDTO", new Member.rqFindId());
        return "/SignUp/FindId";
    }

    //ID 찾기
    @PostMapping("/loginform/findidform/findid")
    @ResponseBody
    public Member.rpFindId findId(Member.rqFindId rqFindId){
        Member.rpFindId rpFindId = signUpService.findIdSearch(rqFindId);
        return rpFindId;
    }

    //ID 찾기 결과 확인 페이지 이동
    @GetMapping("/loginform/findidform/checkid")
    public String checkId(String emailId, String platform, Model model) {
        model.addAttribute("emailId", emailId);
        model.addAttribute("platform", platform);
        return "/SignUp/FindIdResult";
    }
    /////////////////////////////////////////////////// PWD 찾기(재설정) ///////////////////////////////////////////////////
    //PWD 찾기 이동
    @GetMapping("/loginform/findpwdform")
    public String findPwdForm(Model model) {
        //바인딩
        model.addAttribute("memberDTO", new Member.rqFindPwd());
        return "/SignUp/FindPwd";
    }

    //PW 재설정을 위한 정보확인
    @PostMapping("/loginform/findpwdform/findpwd")
    @ResponseBody
    public String resetPwd(Member.rqFindPwd rqFindPwd) {
        String findPwd = signUpService.findPwdSearch(rqFindPwd);
        System.out.println(findPwd);
        return findPwd;
    }

    //PWD 재설정 페이지 이동
    @GetMapping("/loginform/findpwdform/resetpwdform")
    public String resetPwdForm(String emailId, Model model) {
        //바인딩
        model.addAttribute("emailId", emailId);
        return "/SignUp/ResetPwd";
    }

    //PWD 재설정
    @PostMapping("/loginform/findpwdform/resetpwdform/resetpwd")
    public String resetPwd(Member.rqResetPwd rqResetPwd){
        signUpService.resetPwd(rqResetPwd, passwordEncoder);
        return "/Main";
    }
}