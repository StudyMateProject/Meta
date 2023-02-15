package com.study.soju.service;

import com.study.soju.entity.Member;
import com.study.soju.repository.MemberRepository;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Builder
@Service
public class SignUpService implements UserDetailsService {
    // 멤버 DB
    @Autowired
    MemberRepository memberRepository;

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

    // 로그인 유저 닉네임 조회
    public Member.rpNickname memberNickname(String emailId) { // 1. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 2. 1에서 파라미터로 받아온 아이디로 로그인 유저를 조회하고, 조회된 값을 받아온다.
        Member member = memberRepository.findByEmailId(emailId);
        // 3. 2에서 조회된 Entity를 DTO로 변환한다.
        Member.rpNickname rpNickname = new Member.rpNickname(member);
        // 4. 3에서 변환된 DTO를 반환한다.
        return rpNickname;
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
    public Member.rpMetaProfile metaProfile(String emailId) { // 5. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 6. 5에서 파라미터로 받아온 아이디로 로그인 유저를 조회하고, 조회된 값을 받아온다.
        Member member = memberRepository.findByEmailId(emailId);
        // 7. 6에서 조회된 Entity를 DTO로 변환한다.
        Member.rpMetaProfile rpMetaProfile = new Member.rpMetaProfile(member);
        // 8. 6에서 변환된 DTO를 반환한다.
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
