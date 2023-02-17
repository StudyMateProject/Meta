package com.study.soju.service;

import com.study.soju.dto.MailKeyDTO;
import com.study.soju.entity.Member;
import com.study.soju.repository.MemberRepository;
import lombok.Builder;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Builder
@Service
public class SignUpService implements UserDetailsService {
    // 멤버 DB
    @Autowired
    MemberRepository memberRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //아이디 중복체크
    public String checkEmailId(String emailId) {

        Member member = memberRepository.findByEmailId(emailId);
        if( member != null ) {
            return "no";
        } else {
            // MailKeyDTO불러와서 사용
            String mailKey = new MailKeyDTO().getKey(7, false);

            //Mail Server 설정
            String charSet = "UTF-8"; // 사용할 언어셋
            String hostSMTP = "smtp.naver.com"; // 사용할 SMTP
            String hostSMTPid = "sksh0000@naver.com"; // 사용할 SMTP에 해당하는 ID - 이메일 형식
            String hostSMTPpwd = "akzmsjxndhcbfgv5"; // 사용할 ID에 해당하는 PWD

            // 가장 중요한 TLS설정 - 이것이 없으면 신뢰성 에러가 나온다
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // 보내는 사람 E-Mail, 제목, 내용
            String fromEmail = "sksh0000@naver.com"; // 보내는 사람 email - hostSMTPid와 동일하게 작성
            String fromName = "관리자"; // 보내는 사람 이름
            String subject = "[Study with me] 이메일 인증번호 발송 안내입니다."; // 제목

            // 받는 사람 E-Mail 주소
            String mail = emailId; // 받는 사람 email

            try {
                HtmlEmail email = new HtmlEmail(); // Email 생성
                email.setDebug(true);
                email.setCharset(charSet); // 언어셋 사용
                email.setSSL(true);
                email.setHostName(hostSMTP); // SMTP 사용
                email.setSmtpPort(587);	// SMTP 포트 번호 입력

                email.setAuthentication(hostSMTPid, hostSMTPpwd); // 메일 ID, PWD
                email.setTLS(true);
                email.addTo(mail); // 받는 사람
                email.setFrom(fromEmail, fromName, charSet); // 보내는 사람
                email.setSubject(subject); // 제목
                email.setHtmlMsg(
                        "<p>" + "[메일 인증 안내입니다.]" + "</p>" +
                                "<p>" + "Study with me를 사용해 주셔서 감사드립니다." + "</p>" +
                                "<p>" + "아래 인증 코드를 '인증번호'란에 입력해 주세요." + "</p>" +
                                "<p>" + mailKey + "</p>"); // 본문 내용
                email.send(); // 메일 보내기
                // 메일 보내기가 성공하면 메일로 보낸 랜덤키를 콜백 메소드에도 전달
                return mailKey;
            } catch (Exception e) {
                System.out.println(e);
                // 메일 보내기가 실패하면 "no"를 콜백 메소드에 전달
                return "no";
            }
        }
    }

    //핸드폰번호 중복체크
    public String checkPhone (String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber);
        if ( member != null ) {
            return "no";
        } else {
            return "yes";
        }
    }

    // 회원가입
    public Member.rpJoinMember joinMember(Member.rqJoinMember rqJoinMember, PasswordEncoder passwordEncoder) { // 3. 파라미터로 컨트롤러에서 넘어온 DTO와 비밀번호 암호화 메소드를 받아온다.
        // 4. 3에서 파라미터로 받아온 DTO를 Entity로 변환하면서 3에서 파라미터로 같이 받아온 비밀번호 암호화 메소드를 파라미터로 넘겨준다.
        Member joinMember = rqJoinMember.toEntity(passwordEncoder);
        // 8. 4에서 변환된 Entity로 유저를 저장하고, 저장한 값을 받아온다.
        Member member = memberRepository.save(joinMember);
        // 9. 8에서 저장하고 받아온 Entity를 DTO로 변환한다.
        Member.rpJoinMember rpJoinMember = new Member.rpJoinMember(member);
        // 10. 9에서 변환된 DTO를 반환한다.
        return rpJoinMember;
    }

    // 로그인 유저 닉네임 및 프로플 사진 조회
    public Member.rpNickImage memberNickImage(String emailId) { // 1. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 2. 1에서 파라미터로 받아온 아이디로 로그인 유저를 조회하고, 조회된 값을 받아온다.
        Member member = memberRepository.findByEmailId(emailId);
        // 3. 2에서 조회된 Entity를 DTO로 변환한다.
        Member.rpNickImage rpNickImage = new Member.rpNickImage(member);
        // 4. 3에서 변환된 DTO를 반환한다.
        return rpNickImage;
    }

    // 로그인 유저 메타 프로필 작성용 조회
    public Member.rpMetaProfile metaProfile(String emailId) { // 1. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 2. 1에서 파라미터로 받아온 아이디로 로그인 유저를 조회하고, 조회된 값을 받아온다.
        Member member = memberRepository.findByEmailId(emailId);
        // 3. 2에서 조회된 Entity를 DTO로 변환한다.
        Member.rpMetaProfile rpMetaProfile = new Member.rpMetaProfile(member);
        // 4. 3에서 변환된 DTO를 반환한다.
        return rpMetaProfile;
    }

    // 로그인시 인증 방식 - Spring Security에서 DB로 변경한다.
    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException { // 3. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 4. 3에서 파라미터로 받아온 아이디로 유저를 조회하고, 조회된 값을 받아온다.
        Member member = memberRepository.findByEmailId(emailId);
        // 5. 4에서 조회된 값이 있는지 체크한다.
        // 5-1. 조회된 값이 없는 경우
        if ( member == null ) {
            // 예외처리
            throw new UsernameNotFoundException(emailId);
        }
        // 5-2. 조회된 값이 있는 경우
        // 6. 4에서 조회된 유저를 Spring Security에서 제공하는 User에 빌더를 통해 값을 넣어준뒤 SecurityConfig로 반환한다.
        return User.builder()
                .username(member.getEmailId())
                .password(member.getPwd())
                .roles(member.getRoleName())
                .build();
    }
}
