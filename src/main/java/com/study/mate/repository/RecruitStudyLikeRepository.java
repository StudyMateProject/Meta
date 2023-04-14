package com.study.mate.repository;

import com.study.mate.entity.RecruitStudyLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface RecruitStudyLikeRepository extends JpaRepository<RecruitStudyLike, Object> {
    //스터디원 구하기 idx 와 멤버 idx 로 스터디원 구하기 좋아요 검색
    RecruitStudyLike findByLikeIdxAndMemberIdx(long likeIdx, long memberIdx);
    //멤버 idx 로 스터디원 구하기 좋아요 리스트 검색
    List<RecruitStudyLike> findByMemberIdx(long memberIdx);

    //countBy 로 검색할 컬럼의 갯수를 반환한다.
    //countBy 는 예약어처럼 항상 앞에 작성해줘야한다.
    //마치 findBy...처럼
    int countByLikeIdx(long likeIdx);

}
