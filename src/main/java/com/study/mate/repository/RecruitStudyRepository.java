package com.study.mate.repository;

import com.study.mate.entity.RecruitStudy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface RecruitStudyRepository extends JpaRepository<RecruitStudy, Object> {
    //스터디원 구하기 idx 로 스터디원 구하기 검색
    RecruitStudy findByIdx(long idx);
    //작성자이름으로 스터디원 구하기 검색
    RecruitStudy findByWriter(String writer);
    //pageable 로 스터디원 구하기 Page 리스트 검색
    Page<RecruitStudy> findAll(Pageable pageable);
    //스터디 타입으로 스터디원 구하기 리스트 갯수 검색
    Long countByStudyType(String studyType);
    //스터디원 좋아 숫자 업데이트
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RecruitStudy rs SET rs.studyLike = (SELECT COUNT(rsl) FROM RecruitStudyLike rsl WHERE rsl.likeIdx = :idx) WHERE rs.idx = :idx")
    void updateStudyLikeCount(@Param("idx") long idx);

    //페이징 처리를 위해서 스터디원 구하기 idx 순서대로 start 숫자와 end 숫자로 리스트 검색
    @Modifying
    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (ORDER BY idx DESC)AS ranking FROM RecruitStudy)AS ranking WHERE ranking BETWEEN :start AND :end", nativeQuery = true)
    List<RecruitStudy> findRecruitStudyList(@Param("start") int start, @Param("end") int end);

    //메인페이지에 넘겨주기 위해서 스터디원 구하기 좋아요 갯수 와 idx 순서대로 start 숫자와 end 숫자로 리스트 검색
    @Modifying
    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (ORDER BY studyLike DESC, idx DESC)AS ranking FROM RecruitStudy)AS ranking WHERE ranking BETWEEN :start AND :end", nativeQuery = true)
    List<RecruitStudy> findRecruitStudyListRanking(@Param("start") int start, @Param("end") int end);

    //분야별 페이지 검색을 위해서 idx 순서대로 start 숫자와 end 숫자, 스터디 타입으로 리스트 검색
    @Modifying
    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (ORDER BY idx DESC)AS ranking FROM RecruitStudy)AS ranking WHERE ranking BETWEEN :start AND :end AND studyType= :studyType", nativeQuery = true)
    List<RecruitStudy> findRecruitStudyList(@Param("start") int start, @Param("end") int end, @Param("studyType") String studyType);

}
