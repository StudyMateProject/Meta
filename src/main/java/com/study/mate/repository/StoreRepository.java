package com.study.mate.repository;

import com.study.mate.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface StoreRepository extends JpaRepository<Store, Object> {
    //카테고리로 스토어 리스트 검색
    List<Store> findByCategory(String category);
    //상품명으로 스토어 검색
    Store findByItemName(String itemName);
    //스토어 idx 로 스토어 검색
    Store findByStoreIdx(long storeIdx);
    //굿즈로 스토어 카운트 검색
    Long countByGoods(String goods);
    //페이징 처리를 위해서 스토어 idx 순서대로 start 숫자와 end 숫자, 굿즈명 으로 리스트 검색
    @Modifying
    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (ORDER BY storeIdx DESC)AS ranking FROM Store WHERE goods=:goods)AS ranking WHERE ranking BETWEEN :start AND :end", nativeQuery = true)
    List<Store> findStoreList(@Param("start") int start, @Param("end") int end, @Param("goods") String goods);
    //스토어 좋아요 숫자 업데이트
    @Modifying
    @Query("UPDATE Store s SET s.goodsLike = (SELECT COUNT(sl) FROM StoreLike sl WHERE sl.likeIdx = :idx) WHERE s.storeIdx = :idx")
    void updateStoreLikeCount(@Param("idx") long idx);
}
