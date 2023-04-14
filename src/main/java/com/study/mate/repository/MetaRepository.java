package com.study.mate.repository;

import com.study.mate.entity.Meta;
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
public interface MetaRepository extends JpaRepository<Meta, Object> {
    Meta findByIdx(long idx);

    // @Modifying(clearAutomatically = true) - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
    //                                         기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
    //                                         반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
    //                                         "clearAutomatically = true" 옵션은 EntityManager의 1차 캐시를 비워주는 역할을 한다.
    //                                         JPA는 엔티티를 조회한 후 1차 캐시에 저장하므로, 엔티티의 상태 변경 등이 일어날 때 캐시를 비워주지 않으면 예기치 않은 문제가 발생할 수 있다.
    //                                         clearAutomatically 옵션을 사용하면 해당 메서드 실행 후 자동으로 1차 캐시를 비워준다.
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Meta m WHERE m.metaRecruitingPersonnel = :metaRecruitingPersonnel")
    void deleteByMetaRecruitingPersonnel(@Param("metaRecruitingPersonnel") int metaRecruitingPersonnel);

    // 5-1-1-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다. - 방 번호로 조회
    @Query("SELECT m FROM Meta m WHERE m.metaType = :type AND m.idx = :idx")
    // 이름 기반 파라미터 바인딩 - 파라미터에 @Param("") 어노테이션으로 쿼리에 들어갈 값이 어떤 이름으로 지정될 지 정해준다.
    List<Meta> findByIdxList(@Param("type") String metaType, @Param("idx") long idx);

    // 5-2-1-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다. - 방 제목으로 조회
    @Query("SELECT m FROM Meta m WHERE m.metaType = :type AND m.metaTitle LIKE %:title%")
    // 이름 기반 파라미터 바인딩 - 파라미터에 @Param("") 어노테이션으로 쿼리에 들어갈 값이 어떤 이름으로 지정될 지 정해준다.
    List<Meta> findByMetaTitleList(@Param("type") String metaType, @Param("title") String metaTitle);

    // 4-2-3-1. @Query 어노테이션을 사용하여 먼저 COUNT 함수로 수를 조회하고, 그 다음 조회된 값으로 갱신에 사용하는 서브쿼리를 작성한다.
    // @Modifying(clearAutomatically = true) - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
    //                                         기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
    //                                         반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
    //                                         "clearAutomatically = true" 옵션은 EntityManager의 1차 캐시를 비워주는 역할을 한다.
    //                                         JPA는 엔티티를 조회한 후 1차 캐시에 저장하므로, 엔티티의 상태 변경 등이 일어날 때 캐시를 비워주지 않으면 예기치 않은 문제가 발생할 수 있다.
    //                                         clearAutomatically 옵션을 사용하면 해당 메서드 실행 후 자동으로 1차 캐시를 비워준다.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Meta m SET m.metaRecruitingPersonnel = (SELECT COUNT(mr) FROM MetaRoom mr WHERE mr.metaIdx = :idx) WHERE m.idx = :idx")
    void updateMetaRecruitingPersonnelCount(@Param("idx") long idx);

    // 3-1. @Query 어노테이션을 사용하여 방 정보에 방장을 갱신하는데 사용할 쿼리를 작성한다.
    // @Modifying(clearAutomatically = true) - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
    //                                         기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
    //                                         반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
    //                                         "clearAutomatically = true" 옵션은 EntityManager의 1차 캐시를 비워주는 역할을 한다.
    //                                         JPA는 엔티티를 조회한 후 1차 캐시에 저장하므로, 엔티티의 상태 변경 등이 일어날 때 캐시를 비워주지 않으면 예기치 않은 문제가 발생할 수 있다.
    //                                         clearAutomatically 옵션을 사용하면 해당 메서드 실행 후 자동으로 1차 캐시를 비워준다.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Meta m SET m.metaMaster = :metaNickname WHERE idx = :idx")
    int updateMetaMaster(@Param("idx") long idx, @Param("metaNickname") String nickname);

    // 2-1. @Query 어노테이션을 사용하여 삭제에 사용할 쿼리를 작성한다.
    // @Modifying(clearAutomatically = true) - @Query 어노테이션(JPQL Query, Native Query)을 통해 작성된 INSERT, UPDATE, DELETE (SELECT 제외) 쿼리에서 사용되는 어노테이션이다.
    //                                         기본적으로 JpaRepository에서 제공하는 메서드 혹은 메서드 네이밍으로 만들어진 쿼리에는 적용되지 않는다.
    //                                         반환 타입으로는 void 또는 int/Integer만 사용할 수 있다.
    //                                         "clearAutomatically = true" 옵션은 EntityManager의 1차 캐시를 비워주는 역할을 한다.
    //                                         JPA는 엔티티를 조회한 후 1차 캐시에 저장하므로, 엔티티의 상태 변경 등이 일어날 때 캐시를 비워주지 않으면 예기치 않은 문제가 발생할 수 있다.
    //                                         clearAutomatically 옵션을 사용하면 해당 메서드 실행 후 자동으로 1차 캐시를 비워준다.
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Meta m WHERE m.idx = :idx")
    void deleteByIdx(@Param("idx") long idx);
}
