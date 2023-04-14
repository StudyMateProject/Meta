package com.study.mate.repository;

import com.study.mate.entity.RecruitMentorLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface RecruitMentorLikeRepository extends JpaRepository<RecruitMentorLike, Object> {
    //멘토 구하기 idx 와 멤버 idx 로 멘토 구하기 좋아요 검색
    RecruitMentorLike findByLikeIdxAndMemberIdx(long likeIdx, long memberIdx);
    //멤버 idx 로 멘토 구하기 좋아요 리스트 검색
    List<RecruitMentorLike> findByMemberIdx(long memberIdx);
}
