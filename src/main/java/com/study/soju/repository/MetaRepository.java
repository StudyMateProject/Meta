package com.study.soju.repository;

import com.study.soju.entity.Meta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Object> {
    Meta findByMetaIdx(long metaIdx);

    // 5-1-1-1. 방 번호로 조회 - @Query 어노테이션을 사용하여 조회에 사용할 쿼리문을 작성한다.
    @Query("SELECT m FROM Meta m WHERE m.metaType = :type AND m.metaIdx = :idx")
    // 이름 기반 파라미터 바인딩 - 파라미터에 @Param("") 어노테이션으로 쿼리문에 들어갈 값이 어떤 이름으로 지정될 지 정해준다.
    List<Meta> findByMetaIdxList(@Param("type") String metaType, @Param("idx") long metaIdx);

    // 5-2-1-1. 방 제목으로 조회 - @Query 어노테이션을 사용하여 조회에 사용할 쿼리문을 작성한다.
    @Query("SELECT m FROM Meta m WHERE m.metaType = :type AND m.metaTitle LIKE %:title%")
    // 이름 기반 파라미터 바인딩 - 파라미터에 @Param("") 어노테이션으로 쿼리문에 들어갈 값이 어떤 이름으로 지정될 지 정해준다.
    List<Meta> findByMetaTitleList(@Param("type") String metaType, @Param("title") String metaTitle);
}
