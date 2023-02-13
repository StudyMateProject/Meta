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
        // 5. 검색 종류를 체크한다.
        // 5-1. 검색 종류가 방 번호인 경우
        if ( meta.getMetaIdx() != null ) {
            // 5-1-1. 변환된 Entity에서 필요한 데이터들만 DB로 가져가 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
            List<Meta> searchMetaList = metaRepository.findByMetaIdxList(meta.getMetaType(), meta.getMetaIdx());
            // 5-1-2. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
            // .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
            // .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
            // .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
            List<Meta.rpSearchMetaList> rpSearchMetaList = searchMetaList.stream()
                                                                         .map(Meta.rpSearchMetaList::new)
                                                                         .collect(Collectors.toList());
            // 5-1-3. 변환된 List형태의 DTO를 반환한다.
            return rpSearchMetaList;
        // 5-2. 검색 종류가 방 제목인 경우
        } else {
            // 5-2-1. 변환된 Entity에서 필요한 데이터들만 DB로 가져가 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
            List<Meta> searchMetaList = metaRepository.findByMetaTitleList(meta.getMetaType(), meta.getMetaTitle());
            // 5-2-2. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
            // .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
            // .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
            // .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
            List<Meta.rpSearchMetaList> rpSearchMetaList = searchMetaList.stream()
                                                                         .map(Meta.rpSearchMetaList::new)
                                                                         .collect(Collectors.toList());
            // 5-2-3. 변환된 List형태의 DTO를 반환한다.
            return rpSearchMetaList;
        }
    }

    // 입장한 메타 방 조회 후 모집된 인원 증가
    public Meta.rpEntrance entrance(long metaIdx, Member.rpNickImage rpNickImage) { // 8. 파라미터로 컨트롤러에서 넘어온 방 번호와 DTO를 받아온다.
        // 9. 8에서 파라미터로 받아온 방 번호에 해당하는 방을 조회하고, 조회된 값을 받아온다.
        Meta meta = metaRepository.findByMetaIdx(metaIdx);
        // 10. 조회된 값이 있는지 체크한다.
        // 10-1. 조회된 값이 없는 경우 - 해당 방이 없는 경우
        if ( meta == null ) {
            // 10-1-1. 눌값을 반환한다.
            return null;
        // 10-2. 조회된 값이 있는 경우 - 해당 방이 있는 경우
        } else {
            // 11. 8에서 파라미터로 받아온 방 번호와 DTO 값중 닉네임으로 현재 로그인 유저가 해당 방에 참가자로 있는지 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
            MetaRoom metaRoom = metaRoomRepository.findByMetaIdxNickname(metaIdx, rpNickImage.getNickname());
            // 12. 조회된 값이 있는지 체크한다.
            // 12-1. 로그인 유저가 참가자로 있는 경우 - 중복접속 - 새로고침
            if ( metaRoom != null ) {
                // 12-1-1. 9에서 조회하고 받아온 Entity를 DTO로 변환한다.
                Meta.rpEntrance rpEntrance = new Meta.rpEntrance(meta);
                // 12-1-2. 변환된 DTO를 반환한다.
                return rpEntrance;
            // 12-2. 로그인 유저가 참가자로 없는 경우 - 신규접속
            } else {
                // 13. 조회된 값중 참가중인 인원이 정원초과인지 체크한다.
                // 13-1. 조회된 값중 참가중인 인원이 모집인원보다 크거나 같은 경우
                if (meta.getMetaRecruitingPersonnel() >= meta.getMetaPersonnel()) {
                    // 13-1-1. 에러메세지를 작성해 DTO로 변환한다.
                    Meta.rpEntrance rpEntrance = new Meta.rpEntrance("이 방은 정원초과 입니다.");
                    // 13-1-2. 변환된 DTO를 반환한다.
                    return rpEntrance;
                // 13-2. 조회된 값중 모집된 인원이 모집인원보다 작은 경우
                } else {
                    // 13-2-1. 8에서 파라미터로 받아온 방 번호와 DTO를 MetaRoom에 전달하기위해 MetaRoom을 생성한다.
                    MetaRoom metaRoomParticipate = new MetaRoom();
                    // 13-2-2. setter를 사용하여 값을 저장한다.
                    metaRoomParticipate.setMetaIdx(metaIdx);
                    metaRoomParticipate.setMetaNickname(rpNickImage.getNickname());
                    metaRoomParticipate.setMetaProfileImage(rpNickImage.getProfileImage());
                    // 13-2-3. 값이 저장된 MetaRoom을 DB에 저장한다.
                    metaRoomRepository.save(metaRoomParticipate);
                    // 13-2-4. 8에서 파라미터로 받아온 방 번호에 해당하는 참가자 수를 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
                    int participantCount = metaRoomRepository.findByParticipantCount(metaIdx);
                    // 13-2-5. 13-2-4에서 조회하고 받아온 참가자 수를 9에서 조회하고 받아온 Entity에 setter를 통해 저장한다.
                    meta.setMetaRecruitingPersonnel(participantCount);
                    // 13-2-6. 값이 저장된 Entity를 DB에 저장하고, 저장된 값을 받아온다.
                    Meta metaIncrease = metaRepository.save(meta);
                    // 13-2-7. 13-2-6에서 저장하고 받아온 Entity를 DTO로 변환한다.
                    Meta.rpEntrance rpEntrance = new Meta.rpEntrance(metaIncrease);
                    // 13-2-8. 변환된 DTO를 반환한다.
                    return rpEntrance;
                }
            }
        }
    }

    // 방 번호에 해당하는 방에 참가한 유저를 저장 및 참가자 전체 조회
    public List<MetaRoom.rpMetaRoomIdxList> metaRoomParticipant(long metaIdx) { // 16. 파라미터로 컨트롤러에서 넘어온 방 번호와 DTO들을 받아온다.
        // 17. 16에서 파라미터로 받아온 방 번호에 해당하는 참가자를 모두 조회하고, 조회된 값을 받아온다.
        List<MetaRoom> metaRoomIdxList = metaRoomRepository.findByMetaIdx(metaIdx);
        // 18. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
        // .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
        // .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
        // .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
        List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaRoomIdxList.stream()
                                                                            .map(MetaRoom.rpMetaRoomIdxList::new)
                                                                            .collect(Collectors.toList());
        // 19. 변환된 DTO를 반환한다.
        return rpMetaRoomIdxList;
    }

    // 입장한 메타 방 조회 후 모집된 인원 감소 및 참가자 삭제
    public void exit(long metaIdx, Member.rpNickImage rpNickImage) { // 4. 파라미터로 컨트롤러에서 넘어온 방 번호와 DTO를 받아온다.
        // 5. 4에서 파라미터로 받아온 방 번호와 DTO를 MetaRoom에 전달하기위해 MetaRoom을 생성한다.
        MetaRoom metaRoom = new MetaRoom();
        // 6. setter를 사용하여 값을 저장한다.
        metaRoom.setMetaIdx(metaIdx);
        metaRoom.setMetaNickname(rpNickImage.getNickname());
        metaRoom.setMetaProfileImage(rpNickImage.getProfileImage());
        // 7. 값이 저장된 MetaRoom을 DB에서 삭제한다.
        metaRoomRepository.delete(metaRoom);
        // 8. 4에서 파라미터로 받아온 방 번호에 해당하는 방을 조회하고, 조회된 값을 받아온다.
        Meta meta = metaRepository.findByMetaIdx(metaIdx);
        // 9. 4에서 파라미터로 받아온 방 번호에 해당하는 참가자 수를 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
        int participantCount = metaRoomRepository.findByParticipantCount(metaIdx);
        // 10. 9에서 조회하고 받아온 참가자 수를 8에서 조회하고 받아온 Entity에 setter를 통해 저장한다.
        meta.setMetaRecruitingPersonnel(participantCount);
        // 11. 값이 저장된 Entity를 DB에 저장한다.
        metaRepository.save(meta);
    }
}
