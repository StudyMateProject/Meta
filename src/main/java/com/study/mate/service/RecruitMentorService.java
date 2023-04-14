package com.study.mate.service;

import com.study.mate.entity.*;
import com.study.mate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RecruitMentorService {

    @Autowired
    RecruitMentorRepository recruitMentorRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RecruitMentorCommentRepository recruitMentorCommentRepository;

    @Autowired
    RecruitMentorLikeRepository recruitMentorLikeRepository;

    @Autowired
    AlarmRepository alarmRepository;

    //멤버 idx 로 멘토구하기 좋아요를 한 리스트 검색 메서드
    public List<RecruitMentor> likeList(long idx) {
        //멤버 idx 로 멘토구하기 좋아요 리스트 검색
        List<RecruitMentorLike> likeList = recruitMentorLikeRepository.findByMemberIdx(idx);
        //멘토구하기 리스트 생성
        List<RecruitMentor> recruitMentorList = new ArrayList<>();
        //멘토구하기 좋아요 리스트 수만큼 멘토구하기 리스트에 멘토구하기 객체 저장
        for(int i = 0; i < likeList.size(); i++) {
            RecruitMentor recruitMentor = recruitMentorRepository.findByIdx(likeList.get(i).getLikeIdx());
            recruitMentorList.add(recruitMentor);
        }
        //멘토구하기 리스트 리턴
        return recruitMentorList;
    }

    //닉네임으로 emailId를 검색하는 메서드
    public String returnEmailId(String nickname) {
        //닉네임으로 멤버 객체를 검색한 이후 멤버 객체에서 이메일을 리턴
        return memberRepository.findByNickname(nickname).getEmailId();
    }

    //페이징 처리를 위한 열 갯수를 반환하는 메서드
    public int rowTotal() {
        //멘토구하기 리스트 갯수 리턴
        return Long.valueOf(recruitMentorRepository.count()).intValue();
    }

    //페이징 처리를 하는 메서드(멘토구하기 페이지)
    public List<RecruitMentor> recruitMentorListAll(HashMap<String, Integer> map) {
        //페이징 처리를 위해서 start 숫자와 end 숫자를 보내 그 길이 안에 맞는 리스트 검색 후 리턴
        List<RecruitMentor> recruitMentorList = recruitMentorRepository.findRecruitMentorList(map.get("start"), map.get("end"));
        return recruitMentorList;
    }

    //이메일로 닉네임을 검색하는 메서드
    public String returnNickname(String emailId){
        //이메일로 멤버 객체 검색
        Member member = memberRepository.findByEmailId(emailId);
        //닉네임 변수에 멤버 객체의 닉네임을 저장
        String nickname = member.getNickname();
        //닉네임 변수 리턴
        return nickname;
    }

    //멘토구하기 글쓴내용을 저장하는 메서드
    public void writeRecruitMentor(RecruitMentor recruitMentor){
        //파라미터로 받아온 멘토구하기 객체 저장
        recruitMentorRepository.save(recruitMentor);
    }


    //멘토구하기 idx 로 멘토구하기를 검색하는 메서드
    public RecruitMentor findRecruitMentor(Long idx) {
        //멘토구하기 idx 로 멘토구하기 객체 검색 후 리턴
        return recruitMentorRepository.findByIdx(idx);
    }

    //이메일로 멤버 idx 를 반환하는 메서드
    public long returnIdx(String emailId) {
        //멤버 이메일로 멤버 객체 검색
        Member member = memberRepository.findByEmailId(emailId);
        //멤버 idx 리턴
        return member.getIdx();
    }

    //멘토구하기 글을 수정하는 메서드
    public String modify(RecruitMentor recruitMentor){
        //결과값 저장 변수
        String res = "no";
        //만약 멘토구하기 객체가 있다면
        if(recruitMentor != null){
            //멘토구하기 업데이트
            recruitMentorRepository.save(recruitMentor);
            //결과값 수정
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토구하기 글을 삭제하는 메서드
    public String delete(RecruitMentor recruitMentor) {
        //결과값 저장 변수
        String res = "no";
        //만약 멘토구하기 객체가 있다면
        if(recruitMentor != null) {
            //멘토구하기 삭제
            recruitMentorRepository.delete(recruitMentor);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토구하기 idx 로 멘토구하기 댓글 리스트를 반환하는 메서드
    public List<RecruitMentorComment> findCommentList(long commentIdx) {
        //멘토구하기 idx 로 멘토구하기 댓글 리스트틀 검색
        List<RecruitMentorComment> recruitMentorCommentList = recruitMentorCommentRepository.findByCommentIdx(commentIdx);
        //멘토구하기 댓글 리스트 리턴
        return recruitMentorCommentList;
    }

    //멘토구하기 댓글을 저장하는 메서드
    public String saveComment(RecruitMentorComment recruitMentorComment) {
        //결과값 저장 변수
        String res = "no";
        //만약 멘토구하기 댓글 객체가 있다면
        if(recruitMentorComment != null) {
            //멘토구하기 댓글 데이터베이스에 저장
            recruitMentorCommentRepository.save(recruitMentorComment);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토구하기 댓글 idx 로 멘토구하기 댓글을 삭제하는 메서드
    public String deleteComment(long idx) {
        //결과값을 저장하는 변수
        String res = "no";
        //멘토구하기 댓글 idx 로 멘토구하기 댓글 객체 검색
        RecruitMentorComment deleteComment = recruitMentorCommentRepository.findByIdx(idx);
        //만약 멘토구하기 댓글이 있다면
        if(deleteComment != null) {
            //멘토구하기 댓글 삭제처리
            deleteComment.setDeleteCheck(1);
            //멘토구하기 댓글 저장
            recruitMentorCommentRepository.save(deleteComment);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토구하기 댓글을 수정하는 메서드
    public String modifyComment(RecruitMentorComment recruitMentorComment) {
        //결과값을 저장하는 변수
        String res = "no";
        //멘토구하기 댓글 객체를 파라미터로 받아온 멘토구하기 댓글 idx 로 검색
        RecruitMentorComment beforeModify = recruitMentorCommentRepository.findByIdx(recruitMentorComment.getIdx());
        //만약 검색한 멘토구하기 댓글이 있다면
        if(beforeModify != null) {
            //멘토구하기 댓글 내용 변경
            beforeModify.setComment(recruitMentorComment.getComment());
            //멘토구하기 댓글 작성시간 변경
            beforeModify.setWriteDate(recruitMentorComment.getWriteDate());
            //멘토구하기 댓글 업데이트
            recruitMentorCommentRepository.save(beforeModify);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토구하기 좋아요를 눌렀는지 확인하는 메서드
    public RecruitMentor likeUpdate(RecruitMentorLike recruitMentorLike ,Alarm alarm) {
        //멘토구하기 idx 와 멤버 idx 로 멘토구하기 좋아요 객체 검색
        RecruitMentorLike recruitMentorLike1 = recruitMentorLikeRepository.findByLikeIdxAndMemberIdx(recruitMentorLike.getLikeIdx(), recruitMentorLike.getMemberIdx());
        //멘토구하기 객체 생성
        RecruitMentor afterRecruitMentor = null;
        //만약 멘토구하기 좋아요 객체가 없다면
        if (recruitMentorLike1 == null) {
            //멘토구하기 좋아요 저장
            recruitMentorLikeRepository.save(recruitMentorLike);
            //멘토구하기에 좋아요 갯수 업데이트
            recruitMentorRepository.updateMentorLikeCount(recruitMentorLike.getLikeIdx());
            //업데이트된 좋아요 갯수를 받아오기위해 멘토구하기 객체 검색
            afterRecruitMentor = recruitMentorRepository.findByIdx(recruitMentorLike.getLikeIdx());
            //좋아요를 눌렀을때 알람 생성
            alarm.setTitle(alarm.getNickname() + "님 이 좋아요를 눌렀어요");
            //알람 생성
            alarmRepository.save(alarm);
        //멘토구하기 좋아요 객체가 있다면
        }else {
            //멘토구하기 좋아요 삭제
            recruitMentorLikeRepository.delete(recruitMentorLike1);
            //멘토구하기에 좋아요 갯수 업데이트
            recruitMentorRepository.updateMentorLikeCount(recruitMentorLike1.getLikeIdx());
            //업데이트된 좋아요 갯수를 받아오기위해 멘토구하기 객체 검색
            afterRecruitMentor = recruitMentorRepository.findByIdx(recruitMentorLike1.getLikeIdx());
        }
        //멘토구하기 객체 리턴
        return afterRecruitMentor;
    }

    //좋아요 값을 확인해서 변경하기위하 메서드
    public int likeCheck(long likeIdx, long memberIdx) {
        //결과값을 저장할 변수
        int count = 0;
        //멘토구하기 idx 와 멤버 idx 로 멘토구하기 좋아요 객체 검색
        RecruitMentorLike recruitMentorLike = recruitMentorLikeRepository.findByLikeIdxAndMemberIdx(likeIdx, memberIdx);
        //만약 멘토구하기 좋아요 객체가 있다면
        if(recruitMentorLike != null) {
            //결과값 변경
            count = 1;
        }
        //결과값 리턴
        return count;
    }

    //멘토구하기 신청 알림을 보내는 메서드
    public String mentorApply(Alarm alarm) {
        //결과값을 저장할 변수
        String res = "no";
        //알림 타입, 이메일, 닉네임, 멘토구하기 idx 로 알림 객체 검색
        Alarm alarm1 = alarmRepository.findByAlarmTypeAndEmailIdAndNicknameAndRecruitMentorIdx(alarm.getAlarmType(),
                alarm.getEmailId(),
                alarm.getNickname(),
                alarm.getRecruitMentorIdx());
        //만약 알림이 있다면 이미 신청을 한 상태이므로
        if(alarm1 != null) {
            //결과값 변경
            res = "exist";
        //알림이 없다면 신청을 안한 상태이므로
        } else {
            //알림 내용 생성
            alarm.setTitle(alarm.getNickname() + "님 이 멘토 신청을 했습니다.");
            //알림 객체 저장
            alarmRepository.save(alarm);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }
}
