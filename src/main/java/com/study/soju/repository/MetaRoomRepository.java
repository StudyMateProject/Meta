package com.study.soju.repository;

import com.study.soju.entity.MetaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRoomRepository extends JpaRepository<MetaRoom, Object> {
    List<MetaRoom> findByMetaIdx(long metaIdx);
}
