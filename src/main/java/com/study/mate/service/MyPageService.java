package com.study.mate.service;

import com.study.mate.entity.*;
import com.study.mate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Service
public class MyPageService {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    AlarmRepository alarmRepository;
    @Autowired
    RecruitStudyRepository recruitStudyRepository;
    @Autowired
    RecruitMentorRepository recruitMentorRepository;
    @Autowired
    RecruitMenteeRepository recruitMenteeRepository;
    @Autowired
    MeetingRepository meetingRepository;
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

    public List<Alarm> findEmailId(String emailId) {
        return alarmRepository.findByEmailId(emailId);
    }

    //알림 페이지에서 수락 버튼을 눌렀을 때 실행될 메서드
    public int accept(Alarm alarm, String clientEmailId, String writerEmailId) {
        //결과값을 저장하기 위한 변수
        int res = 0;
        //만약 studyIdx 만 값이 들어있고 mentorIdx 와 menteeIdx 가 비어있다면 실행할 내용
        if (alarm.getRecruitStudyIdx() != null && alarm.getRecruitMentorIdx() == null && alarm.getRecruitMenteeIdx() == null) {
            //구하는 사람을 추가하기 위해서 스터디원 구하기 객체 검색
            RecruitStudy recruitStudy = recruitStudyRepository.findByIdx(alarm.getRecruitStudyIdx());
            //만약 아직도 인원을 구하는 중이라면
            if(recruitStudy.getRecruiting() == 0) {
                //진행중인 만남을 위해서 작성자 진행중인 만남이 있는지 검색(여러명을 구할때 작성자가 겹치는걸 방지하기 위해서)
                Meeting meetingWriter = meetingRepository.findByEmailIdAndRecruitStudyIdx(writerEmailId, recruitStudy.getIdx());
                //만약 진행중인 만남에 값이 없다면
                if(meetingWriter == null) {
                    //새로 진행중인 만남을 만들고 값들을 추가한 다음 저장
                    meetingWriter = new Meeting();
                    meetingWriter.setEmailId(writerEmailId);
                    meetingWriter.setRecruitStudyIdx(recruitStudy.getIdx());
                    meetingWriter.setRecruitStudyTitle(recruitStudy.getTitle());
                    meetingWriter.setRecruitStudyImage(recruitStudy.getImage());
                    meetingRepository.save(meetingWriter);
                }
                //진행중인 만남이 없으면 추가하지 않고 그다음 내용실행
                //진행중인 만남을 위해서 신청한 사람의 진행중인 만남이 있는지 검색(중복 신청을 받는것을 방지하기 위해서)
                Meeting meetingClient = meetingRepository.findByEmailIdAndRecruitStudyIdx(clientEmailId, recruitStudy.getIdx());
                //만약 진행중인 만남에 값이 없다면
                if(meetingClient == null) {
                    //새로 진행중인 만남을 만들고 값들을 추가한 다음 저장
                    meetingClient = new Meeting();
                    meetingClient.setEmailId(clientEmailId);
                    meetingClient.setRecruitStudyIdx(recruitStudy.getIdx());
                    meetingClient.setRecruitStudyTitle(recruitStudy.getTitle());
                    meetingClient.setRecruitStudyImage(recruitStudy.getImage());
                    meetingRepository.save(meetingClient);
                } else {
                    //만약 저장된 만남이 있다면 값들을 새로 추가하지않고 중복값으로 처리후 리턴
                    res = 5;
                    return res;
                }
                //진행중인 만남이 추가되었다면
                recruitStudy.setRecruitingPersonnel(recruitStudy.getRecruitingPersonnel() + 1);
                //인원은 추가한 이후에 인원이 꽉차면 더이상 구하지 않게 하기위해서 모집완료로 변경
                if(recruitStudy.getPersonnel() <= recruitStudy.getRecruitingPersonnel()) {
                    recruitStudy.setRecruiting(1);
                }
                //인원 변경 이후 그값을 업데이트
                recruitStudyRepository.save(recruitStudy);
                //알람 삭제
                Alarm deleteAlarm = alarmRepository.findByIdx(alarm.getIdx());
                alarmRepository.delete(deleteAlarm);
                res = 1;
            } else {
                //인원을 다 구했다면 삭제하지 않고 돌아간다.
                res = 4;
            }
            //만약 mentorIdx 만 값이 들어있고 studyIdx 와 menteeIdx 가 비어있다면 실행할 내용
        } else if(alarm.getRecruitMentorIdx() != null && alarm.getRecruitStudyIdx() == null && alarm.getRecruitMenteeIdx() == null) {
            //구하는 사람을 추가하기 위해서 멘토 구하기 객체 검색
            RecruitMentor recruitMentor = recruitMentorRepository.findByIdx(alarm.getRecruitMentorIdx());
            //만약 아직도 인원을 구하는 중이라면
            if(recruitMentor.getRecruiting() == 0) {
                //진행중인 만남을 위해서 작성자 진행중인 만남이 있는지 검색(여러명을 구할때 작성자가 겹치는걸 방지하기 위해서)
                Meeting meetingWriter = meetingRepository.findByEmailIdAndRecruitMentorIdx(writerEmailId, recruitMentor.getIdx());
                //만약 진행중인 만남에 값이 없다면
                if(meetingWriter == null) {
                    //새로 진행중인 만남을 만들고 값들을 추가한 다음 저장
                    meetingWriter = new Meeting();
                    meetingWriter.setEmailId(writerEmailId);
                    meetingWriter.setRecruitMentorIdx(recruitMentor.getIdx());
                    meetingWriter.setRecruitMentorTitle(recruitMentor.getTitle());
                    meetingWriter.setRecruitMentorWriter(recruitMentor.getWriter());
                    meetingRepository.save(meetingWriter);
                }
                //진행중인 만남이 없으면 추가하지 않고 그다음 내용실행
                //진행중인 만남을 위해서 신청한 사람의 진행중인 만남이 있는지 검색(중복 신청을 받는것을 방지하기 위해서)
                Meeting meetingClient = meetingRepository.findByEmailIdAndRecruitMentorIdx(clientEmailId, recruitMentor.getIdx());
                //만약 진행중인 만남에 값이 없다면
                if(meetingClient == null) {
                    //새로 진행중인 만남을 만들고 값들을 추가한 다음 저장
                    meetingClient = new Meeting();
                    meetingClient.setEmailId(clientEmailId);
                    meetingClient.setRecruitMentorIdx(recruitMentor.getIdx());
                    meetingClient.setRecruitMentorTitle(recruitMentor.getTitle());
                    meetingClient.setRecruitMentorWriter(recruitMentor.getWriter());
                    meetingRepository.save(meetingClient);
                } else {
                    //만약 저장된 만남이 있다면 값들을 새로 추가하지않고 중복값으로 처리후 리턴
                    res = 5;
                    return res;
                }
                //진행중인 만남이 추가되었다면
                //인원은 추가한 이후에 인원을 더이상 구하지 않게 하기위해서 모집완료로 변경
                recruitMentor.setRecruiting(1);
                //인원 변경 이후 그값을 업데이트
                recruitMentorRepository.save(recruitMentor);
                //알림 삭제
                Alarm deleteAlarm = alarmRepository.findByIdx(alarm.getIdx());
                alarmRepository.delete(deleteAlarm);
                res = 2;
            } else {
                res = 4;
            }
            //만약 menteeIdx 만 값이 들어있고 studyIdx 와 mentorIdx 가 비어있다면 실행할 내용
        } else if(alarm.getRecruitMenteeIdx() != null && alarm.getRecruitStudyIdx() == null && alarm.getRecruitMentorIdx() == null) {
            //구하는 사람을 추가하기 위해서 멘티 구하기 객체 검색
            RecruitMentee recruitMentee = recruitMenteeRepository.findByIdx(alarm.getRecruitMenteeIdx());
            //만약 아직도 인원을 구하는 중이라면
            if(recruitMentee.getRecruiting() == 0) {
                //진행중인 만남을 위해서 작성자 진행중인 만남이 있는지 검색(여러명을 구할때 작성자가 겹치는걸 방지하기 위해서)
                Meeting meetingWriter = meetingRepository.findByEmailIdAndRecruitMenteeIdx(writerEmailId, recruitMentee.getIdx());
                //만약 진행중인 만남에 값이 없다면
                if(meetingWriter == null) {
                    //새로 진행중인 만남을 만들고 값들을 추가한 다음 저장
                    meetingWriter = new Meeting();
                    meetingWriter.setEmailId(writerEmailId);
                    meetingWriter.setRecruitMenteeIdx(recruitMentee.getIdx());
                    meetingWriter.setRecruitMenteeTitle(recruitMentee.getTitle());
                    meetingWriter.setRecruitMenteeWriter(recruitMentee.getWriter());
                    meetingWriter.setRecruitMenteeImage(recruitMentee.getImage());
                    meetingRepository.save(meetingWriter);
                }
                //진행중인 만남이 없으면 추가하지 않고 그다음 내용실행
                //진행중인 만남을 위해서 신청한 사람의 진행중인 만남이 있는지 검색(중복 신청을 받는것을 방지하기 위해서)
                Meeting meetingClient = meetingRepository.findByEmailIdAndRecruitMenteeIdx(clientEmailId, recruitMentee.getIdx());
                //만약 진행중인 만남에 값이 없다면
                if(meetingClient == null) {
                    //새로 진행중인 만남을 만들고 값들을 추가한 다음 저장
                    meetingClient = new Meeting();
                    meetingClient.setEmailId(clientEmailId);
                    meetingClient.setRecruitMenteeIdx(recruitMentee.getIdx());
                    meetingClient.setRecruitMenteeTitle(recruitMentee.getTitle());
                    meetingClient.setRecruitMenteeWriter(recruitMentee.getWriter());
                    meetingClient.setRecruitMenteeImage(recruitMentee.getImage());
                    meetingRepository.save(meetingClient);
                } else {
                    //만약 저장된 만남이 있다면 값들을 새로 추가하지않고 중복값으로 처리후 리턴
                    res = 5;
                    return res;
                }
                //진행중인 만남이 추가되었다면
                //인원은 추가한 이후에 인원을 더이상 구하지 않게 하기위해서 모집완료로 변경
                recruitMentee.setRecruiting(1);
                //인원 변경 이후 그값을 업데이트
                recruitMenteeRepository.save(recruitMentee);
                //알림 삭제
                Alarm deleteAlarm = alarmRepository.findByIdx(alarm.getIdx());
                alarmRepository.delete(deleteAlarm);
                res = 3;
            } else {
                res = 4;
            }
        }
        //결과값 리턴
        return res;
    }

    //알림 페이지에서 거절버튼을 눌렀을 떄
    public String refuse(Alarm alarm) {
        //결과값을 저장하기 위한 변수
        String res = "no";
        //알림 idx 로 알림 삭제
        Alarm deleteAlarm = alarmRepository.findByIdx(alarm.getIdx());
        alarmRepository.delete(deleteAlarm);
        //삭제 이후 결과값 저장
        res = "yes";
        //결과값 리턴
        return res;
    }

    //알림 페이지에서 x(삭제)버튼을 눌렀을때
    public String delete(Alarm alarm) {
        //결과값을 저장하기 위한 변수
        String res = "no";
        //알림 idx 로 알림 삭제
        Alarm deleteAlarm = alarmRepository.findByIdx(alarm.getIdx());
        alarmRepository.delete(deleteAlarm);
        //삭제 이후 결과값 저장
        res = "yes";
        //결과값 리턴
        return res;
    }

    //본인에게 저장된 알림 갯수를 검색하는 메서드
    public int alarmCount(String emailId) {
        //이메일로 저장된 알림갯수 리턴
        return alarmRepository.countByEmailId(emailId);
    }

    //이메일로 멤버 검색을 위한 메서드
    public Member returnMember(String emailId) {
        //이메일로 멤버 객체를 리턴
        return memberRepository.findByEmailId(emailId);
    }

    //이메일로 멤버 idx 를 검색하는 메서드
    public long returnIdx(String emailId) {
        //이메일로 멤버 객체 리턴
        Member member = memberRepository.findByEmailId(emailId);
        //멤버 idx 를 idx 에 저장
        long idx = member.getIdx();
        //멤버 idx 리턴
        return idx;
    }

    //닉네임으로 멤버 이메일을 검색하는 메서드
    public String returnEmailId(String nickname) {
        //닉네임으로 멤버 이메일 리턴
        return memberRepository.findByNickname(nickname).getEmailId();
    }

    //이메일로 현재 진행중인 만남 리스트를 검색하는 메서드
    public List<Meeting> meetingList(String emailId) {
        //이메일로 현재 진행중인 만남 리스트 리턴
        return meetingRepository.findByEmailId(emailId);
    }
}
