package com.study.mate.repository;

import com.study.mate.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional // UPDATE, DELETE 를 사용할 때 필요한 어노테이션
@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Object> {
    List<CheckList> findByEmailId(String emailId);

    @Query("SELECT cl FROM CheckList cl WHERE cl.idx = :idx AND cl.emailId = :emailId")
    CheckList findByIdxEmailId(@Param("idx") long idx, @Param("emailId") String emailId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CheckList cl SET cl.listCheck = :listCheck WHERE cl.idx = :idx AND cl.emailId = :emailId")
    void updateByListCheck(@Param("idx") long idx, @Param("emailId") String emailId, @Param("listCheck") int listCheck);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CheckList cl SET cl.content = :content WHERE cl.idx = :idx AND cl.emailId = :emailId")
    void updateByIdxEmailId(@Param("idx") long idx, @Param("emailId") String emailId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CheckList cl WHERE cl.idx = :idx AND cl.emailId = :emailId")
    void deleteByIdxEmailId(@Param("idx") long idx, @Param("emailId") String emailId);
}
