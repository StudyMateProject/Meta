package com.study.mate.service;

import com.study.mate.entity.Member;
import com.study.mate.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

@Service
public class MyPageService {
    @Autowired
    MemberRepository memberRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 회원 프로필 조회
    public Member.rpProfile selectProfile(Principal principal){
        String emailId = principal.getName();
        Member member = memberRepository.findByEmailId(emailId);
        Member.rpProfile rpProfile = new Member.rpProfile(member);
        return rpProfile;
    }

    // 회원정보 조회
    public Member.rpModifyMember selectMember(Principal principal){
        String emailId = principal.getName();
        Member member = memberRepository.findByEmailId(emailId);
        Member.rpModifyMember rpModifyMember = new Member.rpModifyMember(member);
        return rpModifyMember;
    }

    // 회원정보 수정
    public void modify(Member.rqModifyMember rqModifyMember){
        MultipartFile imageFile = rqModifyMember.getImageFile();
        String profileImage = rqModifyMember.getProfileImage();
        if( !imageFile.isEmpty() ) {
            profileImage = imageFile.getOriginalFilename();
//            File saveFile = new File("/C:/IntelliJ/images/profile", profileImage); // 윈도우
            File saveFile = new File("/Users/p._.sc/Desktop/SojuProject/image/profile", profileImage); // 맥
            if ( !saveFile.exists() ){
                saveFile.mkdirs();
            }else {
                long time = System.currentTimeMillis();
                profileImage = String.format("%d_%s", time, profileImage);
//                saveFile = new File("/C:/IntelliJ/images/profile", profileImage); // 윈도우
                saveFile = new File("/Users/p._.sc/Desktop/SojuProject/image/profile", profileImage); // 맥
            }
            try {
                imageFile.transferTo(saveFile);
            }catch (IllegalStateException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        rqModifyMember.setProfileImage(profileImage);
        Member member = rqModifyMember.toEntity();
        memberRepository.updateMemberInfo(member.getEmailId(), member.getName(), member.getPhoneNumber(),
                                         member.getAddress(), member.getDetailAddress(), member.getStudyType(),
                                         member.getBirthday(), member.getNickname(), member.getGender(),
                                         member.getSelfIntro(), member.getProfileImage());
    }
}
