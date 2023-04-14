package com.study.mate.repository;

import com.study.mate.entity.RecruitMentorComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitMentorCommentRepository extends JpaRepository<RecruitMentorComment, Object> {
    //멘토 댓글 idx 로 댓글 검색
    RecruitMentorComment findByIdx(long idx);
    //멘토 구하기 idx 로 멘토 구하기 댓글 리스트 검색
    List<RecruitMentorComment> findByCommentIdx(long commentIdx);
}
