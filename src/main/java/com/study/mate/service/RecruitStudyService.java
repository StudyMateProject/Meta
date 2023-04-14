package com.study.mate.service;

import com.study.mate.entity.*;
import com.study.mate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RecruitStudyService {

    @Autowired
    RecruitStudyRepository recruitStudyRepository;

    @Autowired
    RecruitStudyLikeRepository recruitStudyLikeRepository;

    @Autowired
    RecruitStudyCommentRepository recruitStudyCommentRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    AlarmRepository alarmRepository;

    //멤버 idx 로 스터디원구하기중 좋아요를 한 리스트 검색 메서드
    public List<RecruitStudy> likeList(long idx) {
        //멤버 idx 로 스터디원구하기 좋아요 리스트 검색
        List<RecruitStudyLike> likeList = recruitStudyLikeRepository.findByMemberIdx(idx);
        //스터디원구하기 리스트 생성
        List<RecruitStudy> recruitStudyList = new ArrayList<>();
        //스터디원구하기 좋아요 리스트 수만큼 스터디원구하기 리스트에 스터디원구하기 객체 저장
        for(int i = 0; i < likeList.size(); i++) {
            RecruitStudy recruitStudy = recruitStudyRepository.findByIdx(likeList.get(i).getLikeIdx());
            recruitStudyList.add(recruitStudy);
        }
        //스터디원구하기 리스트 리턴
        return recruitStudyList;
    }

    //닉네임으로 emailId를 검색하는 메서드
    public String returnEmailId(String nickname) {
        //닉네임으로 멤버 객체를 검색한 이후 멤버 객체에서 이메일을 리턴
        return memberRepository.findByNickname(nickname).getEmailId();
    }

    //페이징 처리를 위한 열 갯수를 반환하는 메서드
    public int rowTotal() {
        //스터디원구하기 리스트 갯수 리턴
        return Long.valueOf(recruitStudyRepository.count()).intValue();
    }

    //페이징 처리를 위한 스터디타입에 맞는 열 갯수를 반환하는 메서드
    public int rowTotal(String studyType) {
        //스터디타입에 맞는 스터디원구하기 리스트 갯수 반환
        return Long.valueOf(recruitStudyRepository.countByStudyType(studyType)).intValue();
    }

    //페이징 처리를 하는 메서드(메인 페이지)
    public List<RecruitStudy> recruitStudyListAll(int start, int end) {
        //페이징 처리를 위해서 start 숫자와 end 숫자를 보내 그 길이 안에 맞는 리스트 검색 후 리턴
        return recruitStudyRepository.findRecruitStudyListRanking(start, end);
    }

    //피이징 처리를 하는 메서드(어느분야에 관심이있나요 페이지)
    public List<RecruitStudy> recruitStudyListAll(HashMap<String, Integer> map, String studyType) {
        //페이징 처리를 위해서 start 숫자와 end 숫자, 스터디타입을 보내 스터디타입에 맞고 그 길이 안에 맞는 리스트를 검색 후 리턴
        List<RecruitStudy> recruitStudyList = recruitStudyRepository.findRecruitStudyList(map.get("start"), map.get("end"), studyType);
        return recruitStudyList;
    }

    //페이징 처리를 하는 메서드(스터디원구하기 페이지)
    public List<RecruitStudy> recruitStudyListAll(HashMap<String, Integer> map) {
        //페이징 처리를 위헤서 start 숫자와 end 숫자를 보내 그 길이 안에 맞는 리스트 검색 후 리턴
        List<RecruitStudy> recruitStudyList = recruitStudyRepository.findRecruitStudyList(map.get("start"), map.get("end"));
        return recruitStudyList;
    }

    //스터디원구하기 글쓴내용을 저장하는 메서드
    public void writeRecruitStudy(RecruitStudy recruitStudy) {
        //스터디원을 구할때 본인도 들어가기때문에 기본 설정값을 1로 잡아준다.
        recruitStudy.setRecruitingPersonnel(1);
        //스터디원구하기 객체 저장
        recruitStudyRepository.save(recruitStudy);
    }

    //이메일로 닉네임을 검색하는 메서드
    public String returnNickname(String emailId) {
        //이메일로 멤버 객체 검색
        Member member = memberRepository.findByEmailId(emailId);
        //닉네임 변수에 멤버 객체의 닉네임을 저장
        String nickname = member.getNickname();
        //닉네임 변수 리턴
        return nickname;
    }

    //이메일로 멤버 idx 를 반환하는 메서드
    public long returnIdx(String emailId) {
        //멤버 이메일로 멤버 객체 검색
        Member member = memberRepository.findByEmailId(emailId);
        //멤버 idx 리턴
        return member.getIdx();
    }

    //스터디원구하기 idx 로 스터디원구하기 객체를 검색하는 메서드
    public RecruitStudy findRecruitStudy(long idx) {
        //스터디원구하기 idx 로 스터디원구하기 객체 검색
        RecruitStudy recruitStudy = recruitStudyRepository.findByIdx(idx);
        //스터디원구하기 객체 리턴
        return recruitStudy;
    }

    //작성자로 스터디원구하기 객체를 검색하는 메서드
    public RecruitStudy findRecruitStudy(String writer) {
        //작성자로 스터디원구하기 객체 검색 및 리턴
        return recruitStudyRepository.findByWriter(writer);
    }

    //좋아요 값을 확인해서 변경하기위한 메서드
    public int likeCheck(long likeIdx, long memberIdx) {
        //결과값을 저장하는 변수
        int count = 0;
        //스터디원구하기 idx 와 멤버 idx 로 스터디원구하기 좋아요 객체 검색
        RecruitStudyLike recruitStudyLike = recruitStudyLikeRepository.findByLikeIdxAndMemberIdx(likeIdx, memberIdx);
        //만약 스터디원구하기 좋아요 객체가 있다면
        if(recruitStudyLike != null) {
            //결과값 변경
            count = 1;
        }
        //결과값 리턴
        return count;
    }

    //스터디원구하기에 좋아요를 눌렀는지 확인하는 메서드
    public RecruitStudy likeUpdate(RecruitStudyLike recruitStudyLike, Alarm alarm) {
        //스터디원구하기 idx 와 멤버 idx 로 스터디원구하기 좋아요 객체 검색
        RecruitStudyLike recruitStudyLike1 = recruitStudyLikeRepository.findByLikeIdxAndMemberIdx(recruitStudyLike.getLikeIdx(), recruitStudyLike.getMemberIdx());
        //스터디원구하기 객체 생성
        RecruitStudy afterRecruitStudy = null;
        //만약 스터디원구하기 좋아요 객체가 없다면
        if (recruitStudyLike1 == null) {
            //스터디원구하기 좋아요 저장
            recruitStudyLikeRepository.save(recruitStudyLike);
            //스터디원구하기에 좋아요 갯수 업데이트
            recruitStudyRepository.updateStudyLikeCount(recruitStudyLike.getLikeIdx());
            //업데이트된 좋아요 갯수를 받아오기위해 스터디원구하기 객체 검색
            afterRecruitStudy = recruitStudyRepository.findByIdx(recruitStudyLike.getLikeIdx());
            //좋아요를 눌렀을때 알람 생성
            alarm.setTitle(alarm.getNickname() + "님 이 좋아요를 눌렀어요");
            //알람 생성
            alarmRepository.save(alarm);
        //스터디원구하기 좋아요 객체가 있다면
        }else {
            //스터디원구하기 좋아요 삭제
            recruitStudyLikeRepository.delete(recruitStudyLike1);
            //스터디원구하기 좋아요 갯수 업데이트
            recruitStudyRepository.updateStudyLikeCount(recruitStudyLike1.getLikeIdx());
            //업데이트된 좋아요 갯수를 받아오기위해 스터디원구하기 객체 검색
            afterRecruitStudy = recruitStudyRepository.findByIdx(recruitStudyLike1.getLikeIdx());
        }
        //스터디원구하기 객체 리턴
        return afterRecruitStudy;
    }

    //스터디원구하기 글을 수정하는 메서드
    public String modify(RecruitStudy recruitStudy) {
        //결과값 저장 변수
        String res = "no";
        //스터디원구하기 글을 변경하기 이전 스터디원구하기 객체에 저장
        RecruitStudy beforeRecruitStudy = recruitStudyRepository.findByIdx(recruitStudy.getIdx());
        //만약 변경 이전 스터디원구하기 객체의 구해진 인원수가 변경하려는 스터디원구하기 객체의 총 인원수보다 많으면
        if(beforeRecruitStudy.getRecruitingPersonnel() > recruitStudy.getPersonnel()) {
            //결과값 변경
            res = "fail";
        //변경이전 스터디원구하기 객체의 구해진 인원수가 변경하려는 스터디원구하기 객체의 총 인원보다 적거나 같을때
        } else{
            //만약 변경이전 스터디원구하기 객체의 구해진 인원수가 변경하려는 스터디원구하기 객체의 총 인원과 같으면
            if(beforeRecruitStudy.getRecruitingPersonnel() == recruitStudy.getPersonnel()) {
                //모집 상태를 변경
                beforeRecruitStudy.setRecruiting(1);
            //변경 이전 스터디원구하기 객체의 구해진 인원수가 변경하려는 스터디원구하기 객체의 총 인원보다 적으면
            }else{
                //모집상태를 변경
                beforeRecruitStudy.setRecruiting(0);
            }
            //스터디원구하기 제목 변경
            beforeRecruitStudy.setTitle(recruitStudy.getTitle());
            //스터디원구하기 타입 변경
            beforeRecruitStudy.setStudyType(recruitStudy.getStudyType());
            //스터디원구하기 인원수 변경
            beforeRecruitStudy.setPersonnel(recruitStudy.getPersonnel());
            //스터디원구하기 이미지 변경
            beforeRecruitStudy.setImage(recruitStudy.getImage());
            //스터디원구하기 내용 변경
            beforeRecruitStudy.setStudyIntro(recruitStudy.getStudyIntro());
            //스터디원구하기 객체 업데이트
            recruitStudyRepository.save(beforeRecruitStudy);
            //결과값 변경
            res = "success";
        }
        //결과값 리턴
        return res;
    }

    //스터디원구하기 글을 삭제하는 메서드
    public String delete(RecruitStudy recruitStudy) {
        //결과값 저장 변수
        String res = "no";
        //만약 스터디원구하기 객체가 있다면
        if(recruitStudy != null) {
            //스터디원구하기 삭제
            recruitStudyRepository.delete(recruitStudy);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //스터디원구하기 idx 로 스터디원구하기 댓글 리스트를 반환하는 메서드
    public List<RecruitStudyComment> findCommentList(long commentIdx) {
        //스터디원구하기 idx 로 스터디원구하기 댓글 리스트를 검색
        List<RecruitStudyComment> recruitStudyCommentList = recruitStudyCommentRepository.findByCommentIdx(commentIdx);
        //스터디원구하기 댓글 리스트 리턴
        return recruitStudyCommentList;
    }

    //스터디원구하기 댓글을 저장하는 메서드
    public String saveComment(RecruitStudyComment recruitStudyComment, Alarm alarm) {
        //결과값을 저장하는 변수
        String res = "no";
        //만약 스터디원구하기 댓글 객체가 있다면
        if (recruitStudyComment != null) {
            //스터디원구하기 댓글 데이터베이스에 저장
            recruitStudyCommentRepository.save(recruitStudyComment);
            //스터디원구하기 댓글 알림 내용 생성
            alarm.setTitle(alarm.getNickname() + "님 이 댓글을 달았어요");
            //스터디원구하기 댓글 알림 저장
            alarmRepository.save(alarm);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //스터디원구하기 댓글 idx 로 스터디원구하기 댓글을 삭제하는 메서드
    public String deleteComment(long idx) {
        //결과값을 저장하는 변수
        String res = "no";
        //스터디원구하기 댓글 idx 로 스터디원구하기 댓글 객체 검색
        RecruitStudyComment deleteComment = recruitStudyCommentRepository.findByIdx(idx);
        //만약 스터디원구하기 댓글이 있다면
        if(deleteComment != null) {
            //스터디원구하기 댓글 삭제처리
            deleteComment.setDeleteCheck(1);
            //스터디원구하기 댓글 저장
            recruitStudyCommentRepository.save(deleteComment);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //스터디원구하기 댓글을 수정하는 메서드
    public String modifyComment(RecruitStudyComment recruitStudyComment) {
        //결과값을 저장하는 변수
        String res = "no";
        //스터디원구하기 댓글 객체를 파라미터로 받아온 스터디원구하기 댓글 idx 로 검색
        RecruitStudyComment beforeModify = recruitStudyCommentRepository.findByIdx(recruitStudyComment.getIdx());
        //만약 검색한 스터디원구하기 댓글이 있다면
        if(beforeModify != null) {
            //스터디원구하기 댓글 내용 변경
            beforeModify.setComment(recruitStudyComment.getComment());
            //스터디원구하기 댓글 작성시간 변경
            beforeModify.setWriteDate(recruitStudyComment.getWriteDate());
            //스터디원구하기 댓글 업데이트
            recruitStudyCommentRepository.save(beforeModify);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //스터디원구하기 신청 알림을 보내는 메서드
    public String studyApply(Alarm alarm) {
        //결과값을 저장할 변수
        String res = "no";
        //알람 타입, 이메일, 닉네임, 스터디원구하기 idx 로 알림 객체 검색
        Alarm alarm1 = alarmRepository.findByAlarmTypeAndEmailIdAndNicknameAndRecruitStudyIdx(alarm.getAlarmType(),
                alarm.getEmailId(),
                alarm.getNickname(),
                alarm.getRecruitStudyIdx());
        //만약 알림이 있다면 이미 신청을 한 상태이므로
        if(alarm1 != null) {
            //결과값 변경
            res = "exist";
        //알림이 없다면 신청을 안한 상태이므로
        } else {
            //알림 내용 생성
            alarm.setTitle(alarm.getNickname() + "님 이 스터디원 신청을 했습니다.");
            //알림 객체 저장
            alarmRepository.save(alarm);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

}
