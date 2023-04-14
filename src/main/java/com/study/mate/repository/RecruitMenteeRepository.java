package com.study.mate.repository;

import com.study.mate.entity.RecruitMentee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface RecruitMenteeRepository extends JpaRepository<RecruitMentee, Object> {
    //멘토프로필 idx 로 멘토프로필 검색
    RecruitMentee findByIdx(Long idx);
    //writer 로 멘토프로필 검색
    RecruitMentee findByWriter(String writer);
    //멘토프로필 좋아요 숫자 업데이트
    @Modifying
    @Query("UPDATE RecruitMentee rm SET rm.studyLike = (SELECT COUNT(rml) FROM RecruitMenteeLike rml WHERE rml.likeIdx = :idx) WHERE rm.idx = :idx")
    void updateMenteeLikeCount(@Param("idx") long idx);
    //페이징 처리를 위해서 idx 순서대로 start 숫자와 end 숫자로 리스트 검색
    @Modifying
    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (ORDER BY idx DESC)AS ranking FROM RecruitMentee)AS ranking WHERE ranking BETWEEN :start AND :end", nativeQuery = true)
    List<RecruitMentee> findRecruitMenteeList(@Param("start") int start, @Param("end") int end);
    //페이징 처리를 위해서 좋아요 갯수순과 idx 순서대로 start 숫자와 end 숫자로 리스트 검색
    @Modifying
    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (ORDER BY studyLike DESC, idx DESC)AS ranking FROM RecruitMentee)AS ranking WHERE ranking BETWEEN :start AND :end", nativeQuery = true)
    List<RecruitMentee> findRecruitMenteeListRanking(@Param("start") int start, @Param("end") int end);
}
