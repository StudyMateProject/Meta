package com.study.mate.repository;

import com.study.mate.entity.Pay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayRepository extends JpaRepository<Pay, Object> {
    //이메일로 결제 리스트 검색
    List<Pay> findByBuyerEmail(String buyerEmail);
    //impUid 로 결제객체 검색
    Pay findByImpUid(String impUid);
    //itemName 으로 결제 리스트 검색
    @Modifying(clearAutomatically = true)
    @Query(value = "SELECT * FROM Pay WHERE itemName=:itemName", nativeQuery = true)
    List<Pay> commentList(@Param("itemName") String itemName);
}
