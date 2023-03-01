package com.study.soju.repository;

import com.study.soju.entity.MetaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional // UPDATE, DELETE 를 사용할 때 필요한 어노테이션
@Repository
public interface MetaRoomRepository extends JpaRepository<MetaRoom, Object> {
    List<MetaRoom> findByMetaIdx(long metaIdx);

    // 11-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다.
    @Query("SELECT m FROM MetaRoom m WHERE m.metaIdx = :metaIdx AND m.metaNickname = :nickname")
    MetaRoom findByMetaIdxNickname(@Param("metaIdx") long metaIdx, @Param("nickname") String nickname);

    // 5-1. @Query 어노테이션을 사용하여 삭제에 사용할 쿼리를 작성한다.
    @Query("DELETE FROM MetaRoom m WHERE m.metaIdx = :metaIdx AND m.metaNickname = :metaNickname")
    // @Modifying - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
    //              기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
    //              반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
    @Modifying
    void exitMetaRoom(@Param("metaIdx") long metaIdx, @Param("metaNickname") String metaNickname);
}
