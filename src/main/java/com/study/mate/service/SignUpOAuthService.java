package com.study.mate.service;

import com.study.mate.entity.Member;
import com.study.mate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class SignUpOAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Autowired
    MemberRepository memberRepository;

    // 구글 로그인 인증 - 미가입자, 가입자, 중복 가입자 비교
    public Member.rpJoinSocialMember findByJoinGoogleMember(String emailId) { // 42. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 43. 42에서 파라미터로 받아온 아이디로 유저를 조회하고, 조회된 값을 받아온다.
        Member member = memberRepository.findByEmailId(emailId);
        // 44. 조회된 값이 있는지 체크한다.
        // 44-1. 조회된 값이 없는 경우 - 미가입자
        if ( member == null ) {
            // 44-1-1. 눌값을 반환한다.
            return null;
        // 44-2. 조회된 값이 있는 경우 - 구글 가입자 or 타 플랫폼 가입자
        } else {
            // 45. 42에서 파라미터로 받아온 아이디와 "google"로 지정한 플랫폼으로 유저를 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
            Member googleMember = memberRepository.findBySocialMember(emailId, "google");
            // 46. 조회된 값이 있는지 체크한다.
            // 46-1. 조회된 값이 없는 경우 - 구글 이메일을 사용하여 다른 플랫폼으로 가입한 유저
            if ( googleMember == null ) {
                // 46-1-1. 에러 메시지를 작성해 DTO로 변환한다.
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember("해당 유저는 다른 플랫폼으로 가입한 이력이 있습니다.\n로그인 페이지로 이동합니다.");
                // 46-1-2. 46-1-1에서 변환된 DTO를 반환한다.
                return rpJoinSocialMember;
            // 46-2. 조회된 값이 있는 경우 - 구글로 가입한 유저
            } else {
                // 46-2-1. 46에서 조회하고 받아온 Entity를 DTO로 변환한다.
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember(member);
                // 46-2-2. 46-2-1에서 변환된 DTO를 반환한다.
                return rpJoinSocialMember;
            }
        }
    }

    // 네이버 로그인 인증 - 미가입자, 가입자, 중복 가입자 비교
    public Member.rpJoinSocialMember findByJoinNaverMember(Member.rqJoinSocialMember rqJoinSocialMember) { // 3. 파라미터로 컨트롤러에서 넘어온 DTO를 받아온다.
        // 4. 3에서 파라미터로 받아온 DTO를 Entity로 변환한다.
        Member rqMember = rqJoinSocialMember.toEntity();
        // 5. 3에서 변환한 Entity 값 중 이름과, 휴대폰 번호로 유저를 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
        Member member = memberRepository.findByJoinMember(rqMember.getName(), rqMember.getPhoneNumber());
        // 6. 조회된 값이 있는지 체크한다.
        // 6-1. 조회된 값이 없는 경우 - 미가입자
        if ( member == null ) {
            // 6-1-1. 눌값을 반환한다.
            return null;
        // 6-2. 조회된 값이 있는 경우 - 네이버 가입자 or 타 플랫폼 가입자
        } else {
            // 7. 3에서 파라미터로 받아온 아이디와 "naver"로 지정한 플랫폼으로 유저를 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
            Member naverMember = memberRepository.findBySocialMember(rqMember.getEmailId(), "naver");
            // 8. 조회된 값이 있는지 체크한다.
            // 8-1. 조회된 값이 없는 경우 - 네이버 이메일을 사용하여 다른 플랫폼으로 가입한 유저
            if (naverMember == null) {
                // 8-1-1. 에러 메시지를 작성해 DTO로 변환한다.
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember("해당 유저는 다른 플랫폼으로 가입한 이력이 있습니다.\n로그인 페이지로 이동합니다.");
                // 8-1-2. 8-1-1에서 변환된 DTO를 반환한다.
                return rpJoinSocialMember;
            // 8-2. 조회된 값이 있는 경우 - 네이버로 가입한 유저
            } else {
                // 8-2-1. 7에서 조회하고 받아온 Entity를 DTO로 변환한다.
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember(naverMember);
                // 8-2-2. 8-2-1에서 변환된 DTO를 반환한다.
                return rpJoinSocialMember;
            }
        }
    }

    // 소셜 회원가입
    public String socialJoin(Member.rqJoinSocialMember rqJoinSocial) { // 3. 파라미터로 컨트롤러에서 넘어온 DTO를 받아온다.
        // 4. 3에서 파라미터로 받아온 DTO를 Entity로 변환한다.
        Member socialJoin = rqJoinSocial.toEntity();
        // 5. 4에서 변환된 Entity로 방을 저장한다.
        Member member = memberRepository.save(socialJoin);
        // 6. 5에서 저장된 값이 있는지 체크한다.
        // 6-1. 저장된 값이 없는 경우 - 가입 실패
        if ( member == null ) {
            // 6-1-1. no를 반환한다.
            return "no";
        // 6-2. 저장된 값이 있는 경우 - 가입 성공
        } else {
            // 6-2-1. yes를 반환한다.
            return "yes";
        }
    }

    // 소셜 로그인시 인증 방식
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException { // 1. 파라미터로 OAuth2UserRequest를 받아온다.
                                                                                                     // OAuth2UserRequest - OAuth 2.0 프로토콜을 사용하여 인증하는데 사용되는 정보를 포함하는 객체
        // 2. OAuth2UserService 인터페이스의 구현체인 DefaultOAuth2UserService 객체를 생성한다.
        //    OAuth2UserService를 통해 OAuth2User 객체를 가져온다.
        //    DefaultOAuth2UserService - OAuth2UserService의 구현체이다.
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        // 3. 2에서 생성한 delegate 객체를 사용해 OAuth2 로그인을 처리하는 OAuth2User 객체를 생성한다.
        //    1에서 가져온 OAuth2User 객체에서 OAuth2UserRequest를 가져오는 코드로 소셜 로그인 진행중인 유저 정보를 가져온다.
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 4. 현재 로그인 진행중인 서비스를 구분하는 코드로 유저가 소셜 로그인 진행중인 플랫폼 이름을 가져온다. - ex) navar, google
        String registrationId = userRequest.getClientRegistration()
                                           .getRegistrationId();

        // 5. OAuth2 로그인 진행시 키가 되는 필드값 - Primary Key와 같은 의미이다.
        // 구글의 경우 기본적으로 코드("sub")를 지원하지만, 네이버 카카오 등은 기본 지원하지 않는다.
        // 그러기에 네이버 카카도 등은 따로 로그인 진행시 키가 되는 필드값을 찾아서 작성해야 한다.
//        String userNameAttributeName = userRequest.getClientRegistration()
//                                                  .getProviderDetails()
//                                                  .getUserInfoEndpoint()
//                                                  .getUserNameAttributeName();
        // 5. 하지만 여기서는 기본적으로 제공되는 키를 사용하는게 아닌 email 키를 사용해 구분지을 것이기에, 키가 되는 필드값을 email로 지정한다.
        String userNameAttributeName = "email";

        // 6. 4에서 가져온 플랫폼과 5에서 지정한 필드값과 3에서 가져온 OAuth2User의 attribute를 DTO에 전달하고 생성한다.
        Member.oauthAttributes oauthGoogle = Member.oauthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 13. 6에서 생성한 DTO를 Entity로 변환한다.
        Member member = oauthGoogle.toEntity();
        // 14. 13에서 변환된 Entity 값 중 아이디로 유저를 조회하고, 조회된 값을 받아온다.
        Member socialLoginMember = memberRepository.findByEmailId(member.getEmailId());

        // 15. 14에서 조회된 값 중 Spring Security 권한 값을 가진 SimpleGrantedAuthority 객체와 6에서 생성한 DTO를 사용해 DefaultOAuth2User 객체를 생성하고 반환한다.
        // 15. 반환하는 객체는 DefaultOAuth2User 타입으로, 생성자에 3개의 파라미터를 전달하여 객체를 생성한다.
        //     첫번째 파라미터는 Collection 타입의 객체를 전달하여 권한(Authority)을 지정하는데, 여기서는 14에서 조회된 값 중 Spring Security 권한(roleName) 값을 기반으로 SimpleGrantedAuthority 객체를 생성하여 전달한다.
        //     두번째 파라미터는 소셜 로그인 과정에서 받아오는 유저 정보로, 여기서는 6에서 생성한 DTO 값 중 유저 정보(attributes)를 가져온다.
        //     세번째 파라미터는 사용자 정보에서 이메일 정보를 가져오는 키가 되는 필드값으로, 여기서는 6에서 생성한 DTO 값 중 필드값(nameAttributeKey)을 가져온다.
        //     이제 생성된 DefaultOAuth2User 객체를 반환하여, 로그인 과정에서 유저 정보를 제공한다.
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(socialLoginMember.getRoleName())), // Spring Security 권한
                                                                                      oauthGoogle.getAttributes(), // 유저 정보 Map
                                                                                      oauthGoogle.getNameAttributeKey()); // 필드값
    }
}