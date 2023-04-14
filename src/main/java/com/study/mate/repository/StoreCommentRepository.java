package com.study.mate.repository;

import com.study.mate.entity.StoreComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface StoreCommentRepository extends JpaRepository<StoreComment, Object> {
    //스토어 댓글 idx 로 스토어 댓글 검색
    StoreComment findByIdx(long idx);
}
