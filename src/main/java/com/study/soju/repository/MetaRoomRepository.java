package com.study.soju.repository;

import com.study.soju.entity.MetaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRoomRepository extends JpaRepository<MetaRoom, Object> {
    List<MetaRoom> findByMetaIdx(long metaIdx);

    // 11-1. @Query 어노테이션을 사용해 조회에 사용할 쿼리문을 작성한다.
    @Query("SELECT m FROM MetaRoom m WHERE m.metaIdx = :metaIdx AND m.metaNickname = :nickname")
    MetaRoom findByMetaIdxNickname(@Param("metaIdx") long metaIdx, @Param("nickname") String nickname);

    // @Query 어노테이션을 사용해 COUNT함수를 사용할 쿼리문을 작성한다.
    @Query("SELECT COUNT(metaIdx) FROM MetaRoom")
    int findByParticipantCount(@Param("metaIdx") long metaIdx);
}
