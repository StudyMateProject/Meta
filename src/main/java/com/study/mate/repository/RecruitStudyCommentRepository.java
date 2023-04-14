package com.study.mate.repository;

import com.study.mate.entity.RecruitStudyComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitStudyCommentRepository extends JpaRepository<RecruitStudyComment, Object> {
    //스터디원 구하기 댓글 idx 로 스터디원 구하기 댓글 검색
    RecruitStudyComment findByIdx(long idx);
    //스터디원 구하기 idx 로 스터디원 구하기 댓글 갯수 검색
    int countByCommentIdx(long commentIdx);
    //스터디원 구하기 idx 로 스터디원 구하기 댓글 리스트 검색
    List<RecruitStudyComment> findByCommentIdx(long commentIdx);
}
