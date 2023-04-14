package com.study.mate.repository;

import com.study.mate.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface MeetingRepository extends JpaRepository<Meeting, Object> {
    //현재 만남 리스트 이메일로 검색
    List<Meeting> findByEmailId(String emailId);
    //이메일과 스터디원구하기 idx 로 검색
    Meeting findByEmailIdAndRecruitStudyIdx(String emailId, long recruitStudyIdx);
    //이메일과 멘토구하기 idx 로 검색
    Meeting findByEmailIdAndRecruitMentorIdx(String emailId, long recruitMentorIdx);
    //이메일과 멘토프로필 idx 로 검색
    Meeting findByEmailIdAndRecruitMenteeIdx(String emailId, long recruitMenteeIdx);
}
