package com.study.mate.repository;

import com.study.mate.entity.MetaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Transactional - 특정 메서드가 하나의 트랜잭션 내에서 실행되도록 하며, 메서드 수행 중 예외가 발생하면 롤백하도록 하는 어노테이션이다.
//                  즉, 모든 데이터베이스 작업이 하나의 트랜잭션으로 처리되도록 하고, 모든 작업이 성공적으로 완료되지 않으면 이전 상태로 롤백할 수 있도록 한다.
//                  이러한 방식으로 데이터 일관성을 유지할 수 있다.
@Transactional // UPDATE, DELETE 를 사용할 때 필요한 어노테이션
@Repository
public interface MetaRoomRepository extends JpaRepository<MetaRoom, Object> {
    List<MetaRoom> findByMetaIdx(long idx);

    MetaRoom findByMetaNickname(String nickname);

    // 11-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다.
    @Query("SELECT m FROM MetaRoom m WHERE m.metaIdx = :metaIdx AND m.metaNickname = :metaNickname")
    MetaRoom findByEntranceMetaRoom(@Param("metaIdx") long idx, @Param("metaNickname") String nickname);

    // 2-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다.
    @Query("SELECT m FROM MetaRoom m WHERE m.metaIdx = :metaIdx AND m.metaNickname = :metaNickname")
    MetaRoom findByMetaIdxNickname(@Param("metaIdx") long idx, @Param("metaNickname") String nickname);

//    // 2-1. @Query 어노테이션을 사용하여 참여중인 방 내부 참여자 명단에 방장을 갱신하는데 사용할 쿼리를 작성한다.
//    // @Modifying(clearAutomatically = true) - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
//    //                                         기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
//    //                                         반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
//    //                                         "clearAutomatically = true" 옵션은 EntityManager의 1차 캐시를 비워주는 역할을 한다.
//    //                                         JPA는 엔티티를 조회한 후 1차 캐시에 저장하므로, 엔티티의 상태 변경 등이 일어날 때 캐시를 비워주지 않으면 예기치 않은 문제가 발생할 수 있다.
//    //                                         clearAutomatically 옵션을 사용하면 해당 메서드 실행 후 자동으로 1차 캐시를 비워준다.
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE MetaRoom mr SET m.metaMaster = null WHERE m.idx = :metaIdx AND m.metaMaster != null")
//    int updateMetaMaster(@Param("metaIdx") long idx);

    // 2-1. @Query 어노테이션을 사용하여 삭제에 사용할 쿼리를 작성한다.
    // @Modifying(clearAutomatically = true) - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
    //                                         기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
    //                                         반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
    //                                         "clearAutomatically = true" 옵션은 EntityManager의 1차 캐시를 비워주는 역할을 한다.
    //                                         JPA는 엔티티를 조회한 후 1차 캐시에 저장하므로, 엔티티의 상태 변경 등이 일어날 때 캐시를 비워주지 않으면 예기치 않은 문제가 발생할 수 있다.
    //                                         clearAutomatically 옵션을 사용하면 해당 메서드 실행 후 자동으로 1차 캐시를 비워준다.
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MetaRoom m WHERE m.metaIdx = :metaIdx AND m.metaNickname = :metaNickname")
    int exitMetaRoom(@Param("metaIdx") long idx, @Param("metaNickname") String nickname);
}
