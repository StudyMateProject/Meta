package com.study.mate.service;

import com.study.mate.entity.*;
import com.study.mate.repository.CheckListRepository;
import com.study.mate.repository.EnterMetaRepository;
import com.study.mate.repository.MetaRepository;
import com.study.mate.repository.MetaRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetaService {
    // 메타 방 DB
    @Autowired
    MetaRepository metaRepository;

    // 메타 방 내부 DB
    @Autowired
    MetaRoomRepository metaRoomRepository;

    // 최근 입장한 메타 방 DB
    @Autowired
    EnterMetaRepository enterMetaRepository;

    @Autowired
    CheckListRepository checkListRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 생성된 메타 방 모두 조회
    public List<Meta.rpMetaList> metaList() {
        // 2. 현재 생성된 메타 방 중에 모집된 인원이 0인 방을 모두 삭제한다. (@Query 어노테이션 사용)
        metaRepository.deleteByMetaRecruitingPersonnel(0);
        // 3. 현재 생성된 메타 방을 모두 조회하고, 조회된 값을 받아온다.
        List<Meta> metaList = metaRepository.findAll();
        // 4. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
        //    .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
        //    .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
        //    .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
        List<Meta.rpMetaList> rpMetaList = metaList.stream()
                                                   .map(Meta.rpMetaList::new)
                                                   .collect(Collectors.toList());
        // 5. 4에서 변환된 List형태의 DTO를 반환한다.
        return rpMetaList;
    }

    // 최근 입장한 방 모두 조회
    public List<Meta> recentEnterMetaList(String nickname) { // 1. 파라미터로 컨트롤러에서 넘어온 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 닉네임으로 최근 입장한 방 본인 목록을 모두 조회하고, 조회된 값을 받아온다.
        List<Meta> recentEnterMetaList = metaRepository.findByRecentEnterMetaList(nickname);
        // 3. 2에서 조회된 값의 길이가 10을 넘는지 체크한다.
        // 3-1. 길이가 10을 넘지 않는 경우
        if ( recentEnterMetaList.size() <= 10 ) {
            // 3-1-1. 추가 작업 없이 2에서 조회한 List를 반환한다.
            return recentEnterMetaList;
        // 3-2. 길이가 10을 넘는 경우
        } else {
            // 3-2-1. 2에서 조회된 List를 0번 배열부터 9번 배열까지 잘라낸다.
            recentEnterMetaList = recentEnterMetaList.subList(0, 10);
            // 3-2-2. 3-2-1에서 잘라낸 List를 반환한다.
            return recentEnterMetaList;
        }
    }

    // 로그인 유저에 해당하는 체크리스트 모두 조회
    public List<CheckList> checkList(String emailId) {
        List<CheckList> checkList = checkListRepository.findByEmailId(emailId);
        if ( checkList == null ) {
            return null;
        } else {
            return checkList;
        }
    }

    // 체크리스트 작성
    public void writeCheckList(CheckList.rqWriteCheckList rqWriteCheckList, String emailId) {
        CheckList checkList = rqWriteCheckList.toEntity(emailId);
        checkListRepository.save(checkList);
    }

    // 체크리스트 체크
    public String checkCheckList(long idx, String emailId) {
        CheckList checkList = checkListRepository.findByIdxEmailId(idx, emailId);
        if ( checkList == null ) {
            return null;
        } else {
            if ( checkList.getListCheck() == 0 ) {
                checkListRepository.updateByListCheck(idx, emailId, 1);
                return "1";
            } else {
                checkListRepository.updateByListCheck(idx, emailId, 0);
                return "0";
            }
        }
    }

    // 체크리스트 수정 페이지
    public CheckList.rpUpdateCheckList updateCheckListForm(long idx, String emailId) {
        CheckList checkList = checkListRepository.findByIdxEmailId(idx, emailId);
        CheckList.rpUpdateCheckList rpUpdateCheckList = new CheckList.rpUpdateCheckList(checkList);
        return rpUpdateCheckList;
    }

    // 체크리스트 수정
    public void updateCheckList(CheckList.rqUpdateCheckList rqUpdateCheckList, String emailId) {
        CheckList checkList = rqUpdateCheckList.toEntity(emailId);
        checkListRepository.save(checkList);
    }

    // 체크리스트 삭제
    public void deleteCheckList(long idx, String emailId) {
        checkListRepository.deleteByIdxEmailId(idx, emailId);
    }

    // 메타 방 생성 후 바로 입장 - 방장 첫 입장
    public Meta.rpCreateMeta createRoom(Meta.rqCreateMeta rqCreateMeta, Sign.rpNickImage rpNickImage) { // 1. 파라미터로 컨트롤러에서 넘어온 방 생성 DTO와 로그인 유저 정보 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 방 생성 DTO 값 중 방 타입이 자습실인지 체크한다.
        // 2-1. 방 타입이 자습실인 경우 - 자습실은 혼자 들어가는 방이기에 방 내부 참여자 명단이 필요하지 않다.
        if ( rqCreateMeta.getMetaType().equals("oneRoom") ) {
            // 2-1-1. 1에서 파라미터로 받아온 방 생성 DTO를 Entity로 변환한다.
            Meta meta = rqCreateMeta.toOneRoom();
            // 2-1-2. 2-1-1에서 변환된 Entity로 방을 저장하고, 저장된 값을 받아온다.
            Meta createMeta = metaRepository.save(meta);
            // 2-1-3. 2-1-2에서 저장하고 받아온 Entity를 DTO로 변환한다.
            Meta.rpCreateMeta rpCreateMeta = new Meta.rpCreateMeta(createMeta);
            // 2-1-4. 2-1-3에서 변환된 DTO를 반환한다.
            return rpCreateMeta;
        // 2-2. 방 타입이 자습실이 아닌 경우 - 스터디룸, 카페룸은 여러명이 들어가는 방이기에 방 내부 참여자 명단이 필요하다.
        } else {
            // 2-2-1. 1에서 파라미터로 받아온 방 생성 DTO를 Entity로 변환한다.
            Meta meta = rqCreateMeta.toEntity();
            // 2-2-2. 2-2-1에서 변환된 Entity로 방을 저장하고, 저장된 값을 받아온다.
            Meta createMeta = metaRepository.save(meta);
            // 2-2-3. 1에서 파라미터로 받아온 로그인 유저 정보 DTO와 2-2-2에서 저장하고 받아온 Entity를 MetaRoom에 전달하기위해 MetaRoom을 생성한다.
            MetaRoom metaRoomParticipate = new MetaRoom(); // 방 내부 참여자 명단
            // 2-2-4. 1에서 파라미터로 받아온 로그인 유저 정보 DTO와 2-2-2에서 저장하고 받아온 Entity를 setter를 통하여 MetaRoom에 전달한다.
            // 2-2-4-1. 2-2-2에서 저장하고 받아온 Entity 값 중 방 번호를 setter를 통하여 MetaRoom에 전달한다.
            metaRoomParticipate.setMetaIdx(createMeta.getIdx());
            // 2-2-4-2. 1에서 파라미터로 받아온 로그인 유저 정보 DTO 값 중 닉네임을 setter를 통하여 MetaRoom에 전달한다.
            metaRoomParticipate.setMetaNickname(rpNickImage.getNickname());
            // 2-2-4-3. 1에서 파라미터로 받아온 로그인 유저 정보 DTO 값 중 프로필 이미지를 setter를 통하여 MetaRoom에 전달한다.
            metaRoomParticipate.setMetaProfileImage(rpNickImage.getProfileImage());
            // 2-2-4-4. 2-2-2에서 저장하고 받아온 Entity 값 중 방장 닉네임을 setter를 통하여 MetaRoom에 전달한다.
            metaRoomParticipate.setMetaMaster(createMeta.getMetaMaster());
            // 2-2-5. 2-2-4에서 값들이 전달된 Entity를 방 내부 참여자 명단에 저장한다.
            metaRoomRepository.save(metaRoomParticipate);
            // 2-2-6. 2-2-2에서 저장하고 받아온 Entity를 DTO로 변환한다.
            Meta.rpCreateMeta rpCreateMeta = new Meta.rpCreateMeta(createMeta);
            // 2-2-7. 2-2-6에서 변환된 DTO를 반환한다.
            return rpCreateMeta;
        }
    }

    // 입장하는 방에 참가중인지 조회 - 방장 첫 입장 체크
    public int entranceMetaRoom(long idx, String nickname) { // 1. 파라미터로 컨트롤러에서 넘어온 방 번호와 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호와 닉네임으로 방 내부 참여자 명단에서 해당 유저를 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
        MetaRoom metaRoom = metaRoomRepository.findByEntranceMetaRoom(idx, nickname);
        // 3. 2에서 조회된 값이 존재하는지 체크한다.
        // 3-1. 조회된 값이 존재하지 않는 경우 - 유저 입장
        if (metaRoom == null) {
            // 3-1-1. 0을 반환한다.
            return 0;
        // 3-2. 조회된 값이 존재하는 경우 - 방장 입장
        } else {
            // 3-2-1. 1을 반환한다.
            return 1;
        }
    }

    // 메타 방 이름 및 분류별 검색
    public List<Meta.rpSearchMetaList> searchMetaList(Meta.rqSearchMetaList rqSearchMetaList) { // 3. 파라미터로 컨트롤러에서 넘어온 방 검색 DTO를 받아온다.
        // 4. 3에서 파라미터로 받아온 DTO를 Entity로 변환한다.
        Meta meta = rqSearchMetaList.toEntity();
        // 5. 4에서 변환된 Entity 값 중 검색 종류를 가져와 체크한다.
        // 5-1. 검색 종류가 방 번호인 경우
        if ( meta.getIdx() != null ) {
            // 5-1-1. 4에서 변환된 Entity 값 중 검색 종류와 idx를 가지고 이에 해당하는 방을 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
            List<Meta> searchMetaList = metaRepository.findByIdxList(meta.getMetaType(), meta.getIdx());
            // 5-1-2. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
            //        .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
            //        .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
            //        .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
            List<Meta.rpSearchMetaList> rpSearchMetaList = searchMetaList.stream()
                                                                         .map(Meta.rpSearchMetaList::new)
                                                                         .collect(Collectors.toList());
            // 5-1-3. 5-1-2에서 변환된 List형태의 DTO를 반환한다.
            return rpSearchMetaList;
        // 5-2. 검색 종류가 방 제목인 경우
        } else {
            // 5-2-1. 4에서 변환된 Entity 값 중 검색 종류와 idx를 가지고 이에 해당하는 방을 조회하고, 조회된 값을 받아온다. (@Query 어노테이션 사용)
            List<Meta> searchMetaList = metaRepository.findByMetaTitleList(meta.getMetaType(), meta.getMetaTitle());
            // 5-2-2. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
            //        .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
            //        .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
            //        .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
            List<Meta.rpSearchMetaList> rpSearchMetaList = searchMetaList.stream()
                                                                         .map(Meta.rpSearchMetaList::new)
                                                                         .collect(Collectors.toList());
            // 5-2-3. 5-2-2에서 변환된 List형태의 DTO를 반환한다.
            return rpSearchMetaList;
        }
    }

    // 방 번호에 해당하는 방 참가 상태 체크
    public int entranceCheck(String nickname) { // 1. 파라미터로 컨트롤러에서 넘어온 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 닉네임으로 해당 유저가 어떤 방이든 내부 참여자 명단에 존재하는지 조회하고, 조회된 값을 받아온다.
        MetaRoom metaRoom = metaRoomRepository.findByMetaNickname(nickname);
        // 3. 2에서 조회된 값이 존재하는지 체크한다.
        // 3-1. 조회된 값이 존재하지 않는 경우 - 로그인 유저가 방 내부 참여자 명단에 존재하지 않는다.
        if ( metaRoom == null ) {
            // 3-1-1. 0을 반환한다.
            return 0;
        // 3-2. 조회된 값이 존재하는 경우 - 로그인 유저가 방 내부 참여자 명단에 존재한다.
        } else {
            // 3-2-1. 1을 반환한다.
            return 1;
        }
    }

    // 입장한 메타 방 조회 후 모집된 인원 증가
    public Meta.rpEntrance newEntrance(long idx, Sign.rpNickImage rpNickImage) { // 1. 파라미터로 컨트롤러에서 넘어온 방 번호와 로그인 유저 정보 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호로 방을 조회하고, 조회된 값을 받아온다.
        Meta meta = metaRepository.findByIdx(idx);
        // 3. 2에서 조회된 값이 존재하는지 체크한다.
        // 3-1. 조회된 값이 존재하지 않는 경우 - 해당 방이 없는 경우
        if ( meta == null ) {
            // 3-1-1. 눌값을 반환한다.
            return null;
        // 3-2. 조회된 값이 존재하는 경우 - 해당 방이 있는 경우
        } else {
            // 4. 2에서 조회된 값 중 참여중인 인원이 정원초과인지 체크한다.
            // 4-1. 참여중인 인원이 모집 인원보다 크거나 같은 경우
            if (meta.getMetaRecruitingPersonnel() >= meta.getMetaPersonnel()) {
                // 4-1-1. 에러 메시지를 작성해 DTO로 변환한다.
                Meta.rpEntrance rpEntrance = new Meta.rpEntrance("해당 방은 정원초과 입니다.");
                // 4-1-2. 4-1-1에서 변환된 DTO를 반환한다.
                return rpEntrance;
            // 4-2. 참여중인 인원이 모집 인원보다 작은 경우
            } else {
                // 4-2-1. 1에서 파라미터로 받아온 방 번호와 로그인 유저 정보 DTO를 MetaRoom에 전달하기위해 MetaRoom을 생성한다.
                MetaRoom metaRoomParticipate = new MetaRoom(); // 방 내부 참여자 명단
                // 4-2-2. 1에서 파라미터로 받아온 방 번호와 로그인 유저 정보 DTO를 setter를 통하여 MetaRoom에 전달한다.
                // 4-2-2-1. 1에서 파라미터로 받아온 방 번호를 setter를 통하여 MetaRoom에 전달한다.
                metaRoomParticipate.setMetaIdx(idx);
                // 4-2-2-2. 1에서 파라미터로 받아온 로그인 유저 정보 DTO 값 중 닉네임을 setter를 통하여 MetaRoom에 전달한다.
                metaRoomParticipate.setMetaNickname(rpNickImage.getNickname());
                // 4-2-2-3. 1에서 파라미터로 받아온 로그인 유저 정보 DTO 값 중 프로필 이미지를 setter를 통하여 MetaRoom에 전달한다.
                metaRoomParticipate.setMetaProfileImage(rpNickImage.getProfileImage());
                // 4-2-2-4. 2에서 조회하고 받아온 Entity 값 중 방장 닉네임을 setter를 통하여 MetaRoom에 전달한다.
                metaRoomParticipate.setMetaMaster(meta.getMetaMaster());
                // 4-2-3. 4-2-2에서 값들이 전달된 Entity를 방 내부 참여자 명단에 저장한다.
                metaRoomRepository.save(metaRoomParticipate);
                // 4-2-4. 1에서 파라미터로 받아온 방 번호로 먼저 방 내부 참여자 명단 수를 조회하고, 그 다음 조회된 값으로 참여중인 인원을 갱신한다. (@Query 어노테이션에 서브쿼리 사용)
                metaRepository.updateMetaRecruitingPersonnelCount(idx);
                // 4-2-5. 1에서 파라미터로 받아온 방 번호로 4-2-4에서 갱신한 방을 다시 조회하고, 조회된 값을 받아온다.
                Meta metaIncrease = metaRepository.findByIdx(idx);
                // 4-2-6. 4-2-5에서 조회하고 받아온 Entity를 DTO로 변환한다.
                Meta.rpEntrance rpEntrance = new Meta.rpEntrance(metaIncrease);
                // 4-2-7. 1에서 파라미터로 받아온 닉네임과 방 번호로 최근 입장한 방 본인 목록에서 현재 입장하는 방을 삭제한다. (@Query 어노테이션 사용)
                enterMetaRepository.deleteByNicknameMetaIdx(rpNickImage.getNickname(), idx);
                // 4-2-8. 4-2-6에서 변환된 DTO를 반환한다.
                return rpEntrance;
            }
        }
    }

    // 입장한 방 재입장 - 새로고침
    public Meta.rpEntrance reEntrance(long idx) { // 1. 파라미터로 컨트롤러에서 넘어온 방 번호를 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호로 방을 조회하고, 조회된 값을 받아온다.
        Meta meta = metaRepository.findByIdx(idx);
        // 3. 2에서 조회된 값이 존재하는지 체크한다.
        // 3-1. 조회된 값이 존재하지 않는 경우 - 새로고침 에러로 방이 삭제된 경우
        if ( meta == null ) {
            // 3-1-1. 눌값을 반환한다.
            return null;
        // 3-1. 조회된 값이 존재하는 경우 - 새로고침 성공으로 방이 존재하는 경우
        } else {
            // 3-2-1. 2에서 조회된 Entity를 DTO로 변환한다.
            Meta.rpEntrance rpEntrance = new Meta.rpEntrance(meta);
            // 3-2-2. 3-2-1에서 변환된 DTO를 반환한다.
            return rpEntrance;
        }
    }

    // 방 번호에 해당하는 방에 참여중인 참가자 전체 조회
    public List<MetaRoom.rpMetaRoomIdxList> metaRoomParticipant(Meta.rpEntrance rpEntrance, Sign.rpNickImage rpNickImage) { // 1. 파라미터로 컨트롤러에서 넘어온 입장한 방 정보 DTO와 로그인 유저 정보 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호에 해당하는 방에 참여중인 참가자들을 모두 조회하고, 조회된 값을 받아온다.
        List<MetaRoom> metaRoomIdxList = metaRoomRepository.findByMetaIdx(rpEntrance.getIdx());
        // 3. List형식의 Entity를 DTO로 변환하는 방법 (stream 방식)
        //    .stream() - List형식의 Entity --> Entity 스트림 - DB에서 가져온 List형식의 Entity를 스트림으로 변환
        //    .map(DTO::new) - Entity 스트림 --> DTO 스트림 - 변한된 Entity 스트림을 DTO클래스의 생성자메소드를 사용해 요소들을 전달하여 DTO로 바꾼뒤 새로운 스트림으로 변환
        //    .collect(Collectors.toList()); - DTO 스트림 --> List형식의 DTO - 변한된 DTO 스트림을 List로 변환
        List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaRoomIdxList.stream()
                                                                            .map(MetaRoom.rpMetaRoomIdxList::new)
                                                                            .collect(Collectors.toList());
        // 5. 4에서 변환된 DTO를 반환한다.
        return rpMetaRoomIdxList;
    }

    // 방장 위임
    public int delegateMaster(long idx, String nickname) { // 1. 파라미터로 컨트롤러에서 넘어온 방 번호와 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호에 해당하는 방에 방장을 1에서 파라미터로 받아온 닉네임으로 갱신하고, 갱신된 결과 값을 받아온다. (@Query 어노테이션 사용)
        int res = metaRepository.updateMetaMaster(idx, nickname);
        // 3. 2에서 갱신된 결과 값을 반환한다.
        return res;
    }

    // 입장한 메타 방 조회 후 참여중인 인원 감소 및 참가자 삭제
    public int exit(long idx, String nickname) { // 1. 파라미터로 컨트롤러에서 넘어온 방 번호와 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호와 닉네임으로 방 내부 참여자 명단에서 해당 유저를 삭제하고, 삭제된 결과 값을 받아온다. (@Query 어노테이션 사용)
        int res = metaRoomRepository.exitMetaRoom(idx, nickname);
        // 3. 1에서 파라미터로 받아온 방 번호로 먼저 방 내부 참여자 명단 수를 조회하고, 그 다음 조회된 값으로 참여중인 인원을 갱신한다. (@Query 어노테이션에 서브쿼리 사용)
        metaRepository.updateMetaRecruitingPersonnelCount(idx);
        // 4. 2에서 삭제된 결과 값을 반환한다.
        return res;
    }

    // 최근 입장한 방 본인 목록에 퇴장하는 방 추가
    public void recentEnterMeta(long idx, String nickname) { // 1. 파라미터로 컨트롤러에서 넘어온 방 번호와 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 닉네임과 방 번호를 EnterMeta의 RequestDTO로 전달해 EnterMeta의 Entity로 변환한다.
        EnterMeta recentEnterMeta = new EnterMeta.rqEnterMeta().toEntity(nickname, idx);
        // 3. 2에서 변환된 Entity로 최근 입장한 방 본인 목록에 저장한다.
        enterMetaRepository.save(recentEnterMeta);
    }

    // 방 삭제
    public void delete(long idx) { // 1. 파라미터로 컨트롤러에서 넘어온 방 번호를 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호로 메타 방에서 해당 방을 삭제한다. (@Query 어노테이션 사용)
        metaRepository.deleteByIdx(idx);
    }
}
