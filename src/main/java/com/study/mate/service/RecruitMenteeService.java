package com.study.mate.service;

import com.study.mate.entity.*;
import com.study.mate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RecruitMenteeService {

    @Autowired
    RecruitMenteeRepository recruitMenteeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RecruitMenteeCommentRepository recruitMenteeCommentRepository;

    @Autowired
    RecruitMenteeLikeRepository recruitMenteeLikeRepository;

    @Autowired
    AlarmRepository alarmRepository;

    //멤버 idx 로 멘토프로필중 좋아요를 한 리스트 검색 메서드
    public List<RecruitMentee> likeList(long idx) {
        //멤버 idx 로 멘토프로필 좋아요 리스트 검색
        List<RecruitMenteeLike> likeList = recruitMenteeLikeRepository.findByMemberIdx(idx);
        //멘토 프로필 리스트 생성
        List<RecruitMentee> recruitMenteeList = new ArrayList<>();
        //멘토 프로필 좋아요 리스트 수만큼 멘토 프로필 리스트에 멘토 프로필 객체 저장
        for(int i = 0; i < likeList.size(); i++) {
            RecruitMentee recruitMentee = recruitMenteeRepository.findByIdx(likeList.get(i).getLikeIdx());
            recruitMenteeList.add(recruitMentee);
        }
        //멘토프로필 리스트 리턴
        return recruitMenteeList;
    }

    //닉네임으로 emailId를 검색하는 메서드
    public String returnEmailId(String nickname) {
        //닉네임으로 멤버 객체를 검색한 이후 멤버 객체에서 이메일을 리턴
        return memberRepository.findByNickname(nickname).getEmailId();
    }

    //패아장 처리를 위한 열 갯수를 반환하는 메서드
    public int rowTotal() {
        //멘토프로필 리스트 갯수 리턴
        return Long.valueOf(recruitMenteeRepository.count()).intValue();
    }

    //페이징 처리를 하는 메서드(메인 페이지)
    public List<RecruitMentee> recruitMenteeListAll(int start, int end) {
        //페이징 처리를 위해서 start 숫자와 end 숫자를 보내 그 길이 안에 맞는 리스트 검색 후 리턴
        return recruitMenteeRepository.findRecruitMenteeList(start, end);
    }

    //페이징 처리를 하는 메서드(멘토프로필 페이지)
    public List<RecruitMentee> recruitMenteeListAll(HashMap<String, Integer> map) {
        //페이징 처리를 위해서 start 숫자와 end 숫자를 보내 그 길이 안에 맞는 리스트 검색 후 리턴
        List<RecruitMentee> recruitMenteeList = recruitMenteeRepository.findRecruitMenteeList(map.get("start"), map.get("end"));
        return recruitMenteeList;
    }

    //멘토 프로필 글쓴내용을 저장하는 메서드
    public void writeRecruitMentee(RecruitMentee recruitMentee) {
        //파라미터로 받아온 멘토프로필 객체 저장
        recruitMenteeRepository.save(recruitMentee);
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


    //멘토프로필 idx 로 멘토프로필을 검색하는 메서드
    public RecruitMentee findRecruitMentee(Long idx) {
        //멘토프로필 idx 로 멘토프로필 객체 검색
        RecruitMentee recruitMentee = recruitMenteeRepository.findByIdx(idx);
        //멘토프로필 객체 리턴
        return recruitMentee;
    }

    //이메일로 멤버 idx 를 반환하는 메서드
    public long returnIdx(String emailId) {
        //멤버 이메일로 멤버 객체 검색
        Member member = memberRepository.findByEmailId(emailId);
        //멤버 idx 리턴
        return member.getIdx();
    }

    //멘토프로필 글을 수정하는 메서드
    public String modify(RecruitMentee recruitMentee){
        //결과값 저장 변수
        String res = "no";
        //만약 멘토프로필 객체가 있다면
        if(recruitMentee != null){
            //멘토프로필 업데이트
            recruitMenteeRepository.save(recruitMentee);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토프로필 글을 삭제하는 메서드
    public String delete(RecruitMentee recruitMentee) {
        //결과값 저장 변수
        String res = "no";
        //만약 멘토프로필 객체가 있다면
        if(recruitMentee != null) {
            //멘토프로필 삭제
            recruitMenteeRepository.delete(recruitMentee);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토프로필 idx 로 멘토프로필 댓글 리스트를 반환하는 메서드
    public List<RecruitMenteeComment> findCommentList(long commentIdx) {
        //멘토프로필 idx 로 멘토프로필 댓글 리스트를 검색
        List<RecruitMenteeComment> recruitMenteeCommentList = recruitMenteeCommentRepository.findByCommentIdx(commentIdx);
        //멘토프로필 댓글 리스트 리턴
        return recruitMenteeCommentList;
    }

    //멘토프로필 댓글을 저장하는 메서드
    public String saveComment(RecruitMenteeComment recruitMenteeComment) {
        //결과값 저장 변수
        String res = "no";
        //만약 멘토프로필 댓글 객체가 있다면
        if(recruitMenteeComment != null) {
            //멘토프로필 댓글 데이터베이스에 저장
            recruitMenteeCommentRepository.save(recruitMenteeComment);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토프로필 댓글 idx 로 멘토프로필 댓글을 삭제하는 메서드
    public String deleteComment(long idx) {
        //결과값을 저장하는 변수
        String res = "no";
        //멘토프로필 댓글 idx 로 멘토프로필 댓글 객체 검색
        RecruitMenteeComment deleteComment = recruitMenteeCommentRepository.findByIdx(idx);
        //만약 멘토프로필 댓글이 있다면
        if(deleteComment != null) {
            //멘토프로필 댓글 삭쩨처리
            deleteComment.setDeleteCheck(1);
            //멘토프로필 댓글 저장
            recruitMenteeCommentRepository.save(deleteComment);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토프로필 댓글을 수정하는 메서드
    public String modifyComment(RecruitMenteeComment recruitMenteeComment) {
        //결과값을 저장하는 변수
        String res = "no";
        //멘토프로필 댓글 객체를 파라미터로 받아온 멘토프로필 댓글 idx 로 검색
        RecruitMenteeComment beforeModify = recruitMenteeCommentRepository.findByIdx(recruitMenteeComment.getIdx());
        //만약 검색한 멘토프로필 댓글이 있다면
        if(beforeModify != null) {
            //멘토프로필 댓글 내용 변경
            beforeModify.setComment(recruitMenteeComment.getComment());
            //멘토프로필 댓글 작성시간 변경
            beforeModify.setWriteDate(recruitMenteeComment.getWriteDate());
            //멘토프로필 댓글 업데이트
            recruitMenteeCommentRepository.save(beforeModify);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //멘토프로필에 좋아요를 눌렀는지 확인하는 메서드
    public RecruitMentee likeUpdate(RecruitMenteeLike recruitMenteeLike, Alarm alarm) {
        //멘토프로필 idx 와 멤버 idx 로 멘토프로필 좋아요 객체 검색
        RecruitMenteeLike recruitMenteeLike1 = recruitMenteeLikeRepository.findByLikeIdxAndMemberIdx(recruitMenteeLike.getLikeIdx(), recruitMenteeLike.getMemberIdx());
        //멘토프로필 객체 생성
        RecruitMentee afterRecruitMentee = null;
        //만약 멘토프로필 좋아요 객체가 없다면
        if (recruitMenteeLike1 == null) {
            //멘토프로필 좋아요 저장
            recruitMenteeLikeRepository.save(recruitMenteeLike);
            //멘토프로필에 좋아요 갯수 업데이트
            recruitMenteeRepository.updateMenteeLikeCount(recruitMenteeLike.getLikeIdx());
            //업데이트된 좋아요 갯수를 받아오기위해 멘토프로필 객체 검색
            afterRecruitMentee = recruitMenteeRepository.findByIdx(recruitMenteeLike.getLikeIdx());
            //좋아요를 눌렀을때 알람 생성
            alarm.setTitle(alarm.getNickname() + "님 이 좋아요를 눌렀어요");
            //알람 생성
            alarmRepository.save(alarm);
        //멘토프로필 좋아요 객체가 있다면
        }else {
            //멘토프로필 좋아요 삭제
            recruitMenteeLikeRepository.delete(recruitMenteeLike1);
            //멘토프로필에 좋아요 갯수 업데이트
            recruitMenteeRepository.updateMenteeLikeCount(recruitMenteeLike1.getLikeIdx());
            //업데이트된 좋아요 갯수를 받아오기위해 멘토프로필 객체 검색
            afterRecruitMentee = recruitMenteeRepository.findByIdx(recruitMenteeLike1.getLikeIdx());
        }
        //멘토프로필 객체 리턴
        return afterRecruitMentee;
    }

    //좋아요 값을 확인해서 변경하기위한 메서드
    public int likeCheck(long likeIdx, long memberIdx) {
        //결과값을 저장할 변수
        int count = 0;
        //멘토프로필 idx 와 멤버 idx 로 멘토프로필 좋아요 객체 검색
        RecruitMenteeLike recruitMenteeLike = recruitMenteeLikeRepository.findByLikeIdxAndMemberIdx(likeIdx, memberIdx);
        //만약 멘토프로필 좋아요 객체가 있다면
        if(recruitMenteeLike != null) {
            //결과갑 변경
            count = 1;
        }
        //결과값 리턴
        return count;
    }

    //멘토프로필 신청 알림을 보내는 메서드
    public String menteeApply(Alarm alarm) {
        //결과값을 저장할 변수
        String res = "no";
        //알람 타입, 이메일, 닉네임, 멘토프로필 idx 로 알림 객체 검색
        Alarm alarm1 = alarmRepository.findByAlarmTypeAndEmailIdAndNicknameAndRecruitMenteeIdx(alarm.getAlarmType(),
                alarm.getEmailId(),
                alarm.getNickname(),
                alarm.getRecruitMenteeIdx());
        //만약 알림이 있다면 이미 신청을 한 상태이므로
        if(alarm1 != null) {
            //결과값 변경
            res = "exist";
        //알림이 없다면 신청을 안한 상태이므로
        } else {
            //알림 내용 생성
            alarm.setTitle(alarm.getNickname() + "님 이 멘티 신청을 했습니다.");
            //알람 객체 저장
            alarmRepository.save(alarm);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }
}
