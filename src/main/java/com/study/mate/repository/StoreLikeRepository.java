package com.study.mate.repository;

import com.study.mate.entity.StoreLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLikeRepository extends JpaRepository<StoreLike, Object> {
    //스토어 idx 와 멤버 idx 로 스토어 좋아요 검색
    StoreLike findByLikeIdxAndMemberIdx(long likeIdx, long memberIdx);
}
