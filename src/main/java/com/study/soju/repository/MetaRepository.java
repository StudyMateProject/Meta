package com.study.soju.repository;

import com.study.soju.entity.Meta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface MetaRepository extends JpaRepository<Meta, Object> {
    Meta findByMetaIdx(long metaIdx);

    // 5-1-1-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다. - 방 번호로 조회
    @Query("SELECT m FROM Meta m WHERE m.metaType = :type AND m.metaIdx = :idx")
    // 이름 기반 파라미터 바인딩 - 파라미터에 @Param("") 어노테이션으로 쿼리에 들어갈 값이 어떤 이름으로 지정될 지 정해준다.
    List<Meta> findByMetaIdxList(@Param("type") String metaType, @Param("idx") long metaIdx);

    // 5-2-1-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다. - 방 제목으로 조회
    @Query("SELECT m FROM Meta m WHERE m.metaType = :type AND m.metaTitle LIKE %:title%")
    // 이름 기반 파라미터 바인딩 - 파라미터에 @Param("") 어노테이션으로 쿼리에 들어갈 값이 어떤 이름으로 지정될 지 정해준다.
    List<Meta> findByMetaTitleList(@Param("type") String metaType, @Param("title") String metaTitle);

    // 4-2-3-1. @Query 어노테이션을 사용하여 먼저 COUNT 함수로 수를 조회하고, 그 다음 조회된 값으로 갱신에 사용하는 서브쿼리를 작성한다.
    @Query("UPDATE Meta m SET m.metaRecruitingPersonnel = (SELECT COUNT(mr) FROM MetaRoom mr WHERE mr.metaIdx = :metaIdx) WHERE m.metaIdx = :metaIdx")
    @Modifying
    void updateMetaRecruitingPersonnelCount(@Param("metaIdx") long metaIdx);
}
