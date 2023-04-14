package com.study.mate.repository;

import com.study.mate.entity.RecruitMentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface RecruitMentorRepository extends JpaRepository<RecruitMentor, Object> {
    //멘토 구하기 idx 로 멘토 구하기 모집글 검색
    RecruitMentor findByIdx(Long idx);
    //작성자로 멘토 구하기 모집글 검색
    RecruitMentor findByWriter(String writer);
    //멘토 구하기 좋아요 숫자 업데이트
    @Modifying
    @Query("UPDATE RecruitMentor rm SET rm.studyLike = (SELECT COUNT(rml) FROM RecruitMentorLike rml WHERE rml.likeIdx = :idx) WHERE rm.idx = :idx")
    void updateMentorLikeCount(@Param("idx") long idx);
    //페이징 처리르 위해서 idx 순서대로 start 숫자와 end 숫자로 리스트 검색
    @Modifying
    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (ORDER BY idx DESC)AS ranking FROM RecruitMentor)AS ranking WHERE ranking BETWEEN :start AND :end", nativeQuery = true)
    List<RecruitMentor> findRecruitMentorList(@Param("start") int start, @Param("end") int end);
}
