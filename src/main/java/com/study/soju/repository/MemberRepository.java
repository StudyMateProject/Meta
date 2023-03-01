package com.study.soju.repository;

import com.study.soju.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Object> {
    Member findByEmailId(String emailId);
    Member findByPhoneNumber(String phoneNumber);

    // 5-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다.
    @Query("SELECT m FROM Member m WHERE m.name = :name AND m.phoneNumber = :phoneNumber")
    Member findByJoinMember(@Param("name") String name, @Param("phoneNumber") String phoneNumber);

    // google : 46-1. / naver : 7-1. @Query 어노테이션을 사용하여 조회에 사용할 쿼리를 작성한다.
    @Query("SELECT m FROM Member m WHERE m.emailId = :emailId AND m.platform = :platform")
    Member findBySocialMember(@Param("emailId") String emailId, @Param("platform") String platform);
}
