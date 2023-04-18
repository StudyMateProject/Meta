package com.study.mate.repository;

import com.study.mate.entity.EnterMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface EnterMetaRepository extends JpaRepository<EnterMeta, Object> {
    // 4-2-7-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다.
    @Query("SELECT em FROM EnterMeta em WHERE em.metaNickname = :metaNickname AND em.metaIdx = :metaIdx")
    EnterMeta findByNicknameMetaIdx(@Param("metaNickname") String nickname, @Param("metaIdx") long metaIdx);

    // 4-2-8-2-1-1. @Query 어노테이션을 사용하여 삭제에 사용할 쿼리를 작성한다.
    // @Modifying(clearAutomatically = true) - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
    //                                         기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
    //                                         반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
    //                                         "clearAutomatically = true" 옵션은 EntityManager의 1차 캐시를 비워주는 역할을 한다.
    //                                         JPA는 엔티티를 조회한 후 1차 캐시에 저장하므로, 엔티티의 상태 변경 등이 일어날 때 캐시를 비워주지 않으면 예기치 않은 문제가 발생할 수 있다.
    //                                         clearAutomatically 옵션을 사용하면 해당 메서드 실행 후 자동으로 1차 캐시를 비워준다.
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM EnterMeta em WHERE em.metaNickname = :metaNickname AND em.metaIdx = :metaIdx")
    void deleteByNicknameMetaIdx(@Param("metaNickname") String metaNickname, @Param("metaIdx") long metaIdx);
}
