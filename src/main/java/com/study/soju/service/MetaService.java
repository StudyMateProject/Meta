package com.study.soju.service;

import com.study.soju.entity.Member;
import com.study.soju.entity.Meta;
import com.study.soju.entity.MetaRoom;
import com.study.soju.repository.MetaRepository;
import com.study.soju.repository.MetaRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetaService {
    // 메타 DB
    @Autowired
    MetaRepository metaRepository;

    // 메타 방 내부 DB
    @Autowired
    MetaRoomRepository metaRoomRepository;

    // 생성된 메타 방 모두 조회
    public List<Meta.rpMetaList> metaList() {
        // 2. 현재 생성된 메타 방을 모두 조회하고, 조회된 값을 받아온다.
        List<Meta> metaList = metaRepository.findAll();
        // 3. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
        // .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
        // .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
        // .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
        List<Meta.rpMetaList> rpMetaList = metaList.stream()
                                                       .map(Meta.rpMetaList::new)
                                                       .collect(Collectors.toList());
        // 4. 변환된 List형태의 DTO를 반환한다.
        return rpMetaList;
    }

    // 메타 방 생성
    public void createRoom(Meta.rqCreateMeta rqCreateMeta) { // 3. 파라미터로 컨트롤러에서 넘어온 DTO를 받아온다.
        // 4. 받아온 DTO를 Entity로 변환한다.
        Meta meta = rqCreateMeta.toEntity();
        // 5. 변환된 Entity로 DB에 저장한다.
        metaRepository.save(meta);
    }

    // 메타 방 이름 및 분류별 검색
    public List<Meta.rpSearchMetaList> searchMetaList(Meta.rqSearchMetaList rqSearchMetaList) { // 3. 파라미터로 컨트롤러에서 넘어온 DTO를 받아온다.
        // 4. 받아온 DTO를 Entity로 변환한다.
        Meta meta = rqSearchMetaList.toEntity();
        // 5. 변환된 Entity에서 필요한 데이터들만 DB로 가져가 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
        List<Meta> searchMetaList = metaRepository.findByMetaList(meta.getMetaType(), meta.getMetaTitle());
        // 6. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
        // .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
        // .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
        // .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
        List<Meta.rpSearchMetaList> rpSearchMetaList = searchMetaList.stream()
                                                                         .map(Meta.rpSearchMetaList::new)
                                                                         .collect(Collectors.toList());
        // 7. 변환된 List형태의 DTO를 반환한다.
        return rpSearchMetaList;
    }

    // 입장한 메타 방 조회 후 모집된 인원 증가
    public Meta.rpEntrance entrance(long metaIdx) { // 3. 파라미터로 컨트롤러에서 넘어온 방 번호를 받아온다.
        // 4. 받아온 방 번호로 조회하고, 조회된 값을 받아온다.
        Meta meta = metaRepository.findByMetaIdx(metaIdx);
        // 5. 조회된 값이 있는지 체크한다.
        // 5-1. 조회된 값이 없는 경우
        if ( meta == null ) {
            // 눌값을 반환한다.
            return null;
        // 5-2. 조회된 값이 있는 경우
        } else {
            // 6. 조회된 값중 모집인원이 정원초과인지 체크한다.
            // 6-1. 조회된 값중 모집된 인원이 모집인원보다 크거나 같은 경우
            if ( meta.getMetaRecruitingPersonnel() >= meta.getMetaPersonnel() ) {
                // 6-1-1. 에러메세지를 DTO로 변환한다.
                Meta.rpEntrance rpEntrance = new Meta.rpEntrance("이 방은 정원초과 입니다.");
                // 6-1-2. 변환된 DTO를 반환한다.
                return rpEntrance;
            // 6-2. 조회된 값중 모집된 인원이 모집인원보다 작은 경우
            } else {
                // 6-2-1. 모집된 인원에 1을 증가시킨다.
                meta.setMetaRecruitingPersonnel(meta.getMetaRecruitingPersonnel() + 1);
                // 6-2-2. 1 증가한 모집된 인원으로 DB에 저장하고, 저장된 값을 받아온다.
                Meta metaIncrease = metaRepository.save(meta);
                // 6-2-3. 저장하고 받아온 Entity를 DTO로 변환한다.
                Meta.rpEntrance rpEntrance = new Meta.rpEntrance(metaIncrease);
                // 6-2-4. 변환된 DTO를 반환한다.
                return rpEntrance;
            }
        }
    }

    // 방 번호에 해당하는 방에 참가한 유저를 저장 및 참가자 전체 조회
    public List<MetaRoom> metaRoomParticipant(long metaIdx, Member.rpNickImage rpNickImage) { // 15. 파라미터로 컨트롤러에서 넘어온 방 번호와 DTO를 받아온다.
        // 16. 받아온 방 번호와 DTO를 MetaRoom에 전달하기위해 MetaRoom을 생성한다.
        MetaRoom metaRoom = new MetaRoom();
        // 17. setter를 사용하여 값을 전달한다.
        metaRoom.setMetaIdx(metaIdx);
        metaRoom.setMetaNickname(rpNickImage.getNickname());
        metaRoom.setMetaProfileImage(rpNickImage.getProfileImage());
        // 18. 값이 전달된 MetaRoom을 DB에 저장한다.
        metaRoomRepository.save(metaRoom);
        // 19. 받아온 방 번호에 해당하는 참가자를 모두 조회하여 반환한다.
        return metaRoomRepository.findByMetaIdx(metaIdx);
    }

    // 입장한 메타 방 조회 후 모집된 인원 감소 및 참가자 삭제
    public void exit(long metaIdx, Member.rpNickImage rpNickImage) { // 3. 파라미터로 컨트롤러에서 넘어온 방 번호와 DTO를 받아온다.
        // 4. 받아온 방 번호로 조회하고, 조회된 값을 받아온다.
        Meta meta = metaRepository.findByMetaIdx(metaIdx);
        // 5. 조회된 값중 모집된 인원에 1을 감소시킨다.
        meta.setMetaRecruitingPersonnel(meta.getMetaRecruitingPersonnel() - 1);
        // 6. 1 감소한 모집된 인원으로 DB에 저장한다.
        metaRepository.save(meta);
        // 7. 받아온 방 번호와 DTO를 MetaRoom에 전달하기위해 MetaRoom을 생성한다.
        MetaRoom metaRoom = new MetaRoom();
        // 8. setter를 사용하여 값을 전달한다.
        metaRoom.setMetaIdx(metaIdx);
        metaRoom.setMetaNickname(rpNickImage.getNickname());
        metaRoom.setMetaProfileImage(rpNickImage.getProfileImage());
        // 9. 값이 전달된 MetaRoom을 DB에서 삭제한다.
        metaRoomRepository.delete(metaRoom);
    }
}
