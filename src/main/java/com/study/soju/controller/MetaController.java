package com.study.soju.controller;

import com.study.soju.entity.Member;
import com.study.soju.entity.Meta;
import com.study.soju.entity.MetaRoom;
import com.study.soju.service.MetaService;
import com.study.soju.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/meta")
public class MetaController {
    // 메타 서비스
    @Autowired
    MetaService metaService;
    // 회원가입 및 로그인 인증 서비스
    @Autowired
    SignUpService signUpService;

    // HTTP 요청에 대한 정보를 담고 있는 클래스
    @Autowired
    HttpServletRequest request;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 메타 메인 페이지
    @GetMapping("")
    public String meta(Principal principal, Model model) throws UnsupportedEncodingException, InterruptedException {
        // 1. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpMetaProfile rpMetaProfile = signUpService.metaProfile(principal.getName());
        // 2. 서비스를 통해 현재 생성된 메타 방을 모두 조회해서, List 형태의 DTO로 반환 받아온다.
        List<Meta.rpMetaList> rpMetaList = metaService.metaList();
        // 3. 2에서 반환받은 List 형태의 DTO를 방의 타입별로 나누기 위해 각 방의 타입마다 List 형태의 DTO를 생성한다.
        // 3-1. 방 타입이 스터디일 경우 - studyRoom List DTO
        List<Meta.rpMetaList> studyList = new ArrayList<>();
        // 3-2. 방 타입이 카페일 경우 - cafeRoom List DTO
        List<Meta.rpMetaList> cafeList = new ArrayList<>();
        // 3-3. 방 타입이 자습일 경우 - oneRoom List DTO
        List<Meta.rpMetaList> oneList = new ArrayList<>();
        // 4. foreach문을 사용하여 2에서 반환받은 List 형태의 DTO에 들어있는 값들을 하나씩 가져온다.
        for ( Meta.rpMetaList metaList : rpMetaList ) {
            // 5. 4에서 가져온 값 중 방 타입을 가져와 체크한다.
            // 5-1. 방 타입이 스터디일 경우
            if ( metaList.getMetaType().equals("studyRoom") ) {
                // 5-1-1. 3-1에서 생성한 studyRoom List DTO에 추가한다.
                studyList.add(metaList);
            // 5-2. 방 타입이 카페일 경우
            } else if ( metaList.getMetaType().equals("cafeRoom") ) {
                // 5-2-1. 3-2에서 생성한 cafeRoom List DTO에 추가한다.
                cafeList.add(metaList);
            // 5-3. 방 타입이 자습일 경우
            } else {
                // 5-3-1. 3-3에서 생성한 oneRoom List DTO에 추가한다.
                oneList.add(metaList);
            }
        }

        // 6. 4에서 foreach문을 사용하여 방 타입별로 값을 새로 추가해 만든 List 형태의 DTO들을 바인딩한다.
        // 6-1. 3-1에서 생성하고 5-1에서 값을 추가한 studyRoom List DTO를 바인딩한다.
        model.addAttribute("studyList", studyList);
        // 6-2. 3-2에서 생성하고 5-2에서 값을 추가한 cafeRoom List DTO를 바인딩한다.
        model.addAttribute("cafeList", cafeList);
        // 6-3. 3-3에서 생성하고 5-3에서 값을 추가한 oneRoom List DTO를 바인딩한다.
        model.addAttribute("oneList", oneList);
        // 7. 1에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
        model.addAttribute("metaProfile", rpMetaProfile);
        // 8. 검색 체크값을 바인딩한다. - 0 : 검색 안했을 경우
        model.addAttribute("check", 0);
        // 9. 검색에 사용할 DTO를 바인딩한다.
        model.addAttribute("metaDTO", new Meta.rqSearchMetaList());
        // 10. 방(자습실) 만들기에 사용할 DTO를 바인딩한다.
//        model.addAttribute("metaDTO", new Meta.rqCreateMeta());
        // 12. 메타 메인 페이지로 이동한다.
        return "Meta/MetaRoom";
    }

    // 방 이름 및 분야별로 검색
    @GetMapping("/search")
    public String searchMeta(Meta.rqSearchMetaList rqSearchMetaList, Principal principal, Model model) { // 1. 파라미터로 form에서 넘어온 방 검색 DTO를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpMetaProfile rpMetaProfile = signUpService.metaProfile(principal.getName());
        // 3. 1에서 파라미터로 받아온 방 검색 DTO를 서비스에 전달한다.
        List<Meta.rpSearchMetaList> rpSearchMetaList = metaService.searchMetaList(rqSearchMetaList);
        // 4. 3에서 반환받은 List 형태의 DTO를 방의 타입별로 나누기 위해 각 방의 타입마다 List를 생성한다.
        // 4-1. 방 타입이 스터디일 경우 - studyRoom List DTO
        List<Meta.rpSearchMetaList> studyList = new ArrayList<>();
        // 4-2. 방 타입이 카페일 경우 - cafeRoom List DTO
        List<Meta.rpSearchMetaList> cafeList = new ArrayList<>();
        // 4-3. 방 타입이 자습일 경우 - oneRoom List DTO
        List<Meta.rpSearchMetaList> oneList = new ArrayList<>();
        // 5. foreach문을 사용하여 3에서 반환받은 List 형태의 DTO에 들어있는 값들을 하나씩 가져온다.
        for ( Meta.rpSearchMetaList searchMetaList : rpSearchMetaList ) {
            // 6. 5에서 가져온 값 중 방 타입을 가져와 체크한다.
            // 6-1. 방 타입이 스터디일 경우
            if ( searchMetaList.getMetaType().equals("studyRoom") ) {
                // 6-1-1. 4-1에서 생성한 studyRoom List DTO에 추가한다.
                studyList.add(searchMetaList);
            // 6-2. 방 타입이 카페일 경우
            } else if ( searchMetaList.getMetaType().equals("cafeRoom") ) {
                // 6-2-1. 4-2에서 생성한 cafeRoom List DTO에 추가한다.
                cafeList.add(searchMetaList);
            // 6-3. 방 타입이 자습일 경우
            } else {
                // 6-3-1. 4-3에서 생성한 oneRoom List DTO에 추가한다.
                oneList.add(searchMetaList);
            }
        }

        // 7. 5에서 foreach문을 사용하여 방 타입별로 값을 새로 추가해 만든 List 형태의 DTO들을 바인딩한다.
        // 7-1. 4-1에서 생성하고 6-1에서 값을 추가한 studyRoom List DTO를 바인딩한다.
        model.addAttribute("studyList", studyList);
        // 7-2. 4-2에서 생성하고 6-2에서 값을 추가한 cafeRoom List DTO를 바인딩한다.
        model.addAttribute("cafeList", cafeList);
        // 7-3. 4-3에서 생성하고 6-3에서 값을 추가한 oneRoom List DTO를 바인딩한다.
        model.addAttribute("oneList", oneList);
        // 8. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
        model.addAttribute("metaProfile", rpMetaProfile);
        // 9. 검색 체크값을 바인딩한다. - 1 : 검색 했을 경우
        model.addAttribute("check", 1);
        // 10. 검색에 사용할 DTO를 바인딩한다.
        model.addAttribute("metaDTO", new Meta.rqSearchMetaList());
        // 11. 메타 메인 페이지로 이동한다.
        return "Meta/MetaRoom";
    }

    // 방 만들기 페이지
    @GetMapping("/createmetaform")
    public String createMetaForm(Model model, Principal principal) {
        // 1. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 2. 1에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 바인딩한다.
        model.addAttribute("metaMaster", rpNickImage.getNickname());
        // 3. 방 만들기에 사용할 DTO를 바인딩한다.
        model.addAttribute("metaDTO", new Meta.rqCreateMeta());
        // 4. 방만들기 페이지로 이동한다.
        return "Meta/CreateMetaForm";
    }

    // 방 만들기
    @GetMapping("/createmetaform/createmeta")
    public String createMeta(Meta.rqCreateMeta rqCreateMeta, Principal principal) { // 1. 파라미터로 form에서 넘어온 방 생성 DTO를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 1에서 파라미터로 받아온 방 생성 DTO와 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
        Meta.rpCreateMeta rpCreateMeta = metaService.createRoom(rqCreateMeta, rpNickImage);
        // 4. 3에서 반환받은 DTO 값 중 방 타입을 체크한다.
        // 4-1. 방 타입이 스터디일 경우
        if ( rpCreateMeta.getMetaType().equals("studyRoom") ) {
            // 4-1-1. 3에서 반환받은 DTO 값 중 방 번호를 가지고 스터디 페이지로 리다이렉트한다.
            return "redirect:/meta/studyroom?idx=" + rpCreateMeta.getIdx();
        // 4-2. 방 타입이 카페일 경우
        } else if ( rpCreateMeta.getMetaType().equals("cafeRoom") ) {
            // 4-2-1. 3에서 반환받은 DTO 값 중 방 번호를 가지고 카페 페이지로 리다이렉트한다.
            return "redirect:/meta/caferoom?idx=" + rpCreateMeta.getIdx();
        // 4-3. 방 타입이 자습일 경우
        } else {
            // 4-3-1. 3에서 반환받은 DTO 값 중 방 번호를 가지고 자습실 페이지로 리다이렉트한다.
            return "redirect:/meta/oneroom?idx=" + rpCreateMeta.getIdx();
        }
    }

    // 스터디룸 입장
    @GetMapping("/studyroom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
    public String studyRoom(@RequestParam long idx, Principal principal, Model model) { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 세션을 사용하기 위하여 HttpServletRequest을 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 4. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값이 존재하는지 체크한다.
        // 4-1. 세션에 값이 존재하는 경우 - 재입장(새로고침)
        if ( session.getAttribute(rpNickImage.getNickname()) != null ) {
            // 4-1-1. 2에서 반환받은 DTO 값 중 닉네임을 서비스에 전달한다.
            int entranceCheck = metaService.entranceCheck(rpNickImage.getNickname());
            // 4-1-1-1. 4-1-1에서 반환받은 값이 어떤것인지 체크한다.
            // 4-1-1-1-1. 반환받은 값이 0인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 X - 퇴장 후 첫 입장
            //            세션은 존재하고 방 내부 참여자 명단에는 존재하지 않다는 것은 퇴장 후 첫 입장을 의미한다.
            if ( entranceCheck == 0 ) {
                // 4-1-1-1-1-1. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                session.removeAttribute(rpNickImage.getNickname());
                // 4-1-1-1-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 스터리룸 페이지로 리다이렉트한다.
                return "redirect:/meta/studyroom?idx=" + idx;
            }
            // 4-1-1-1-2. 반환받은 값이 1인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 O - 재입장(새로고침)
            //            세션도 존재하고 방 내부 참여자 명단에도 존재한다는 것은 재입장(새로고침)을 의미한다.
            // 4-1-1-1-2-1. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
            Meta.rpEntrance rpReEntrance = metaService.reEntrance(idx);
            // 4-1-1-1-2-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
            List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(idx);
            // 4-1-1-1-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
            model.addAttribute("nickImage", rpNickImage);
            // 4-1-1-1-2-4. 4-1-1-1-2-1에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
            model.addAttribute("metaRoom", rpReEntrance);
            // 4-1-1-1-2-5. 4-1-1-1-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
            model.addAttribute("participantList", rpMetaRoomIdxList);
            // 4-1-1-1-2-6. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값을 바인딩한다.
            model.addAttribute("entryCheck", session.getAttribute(rpNickImage.getNickname()));
            // 4-1-1-1-2-7. 스터디룸 페이지로 이동한다.
            return "Meta/StudyRoom";
        // 4-2. 세션에 값이 존재하지 않는 경우 - 첫 입장
        } else {
            // 5. 1에서 파라미터로 받아온 방 번호와 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 서비스에 전달한다.
            int rpEntranceMetaRoom = metaService.entranceMetaRoom(idx, rpNickImage.getNickname());
            // 6. 5에서 반환받은 값이 어떤것인지 체크한다.
            // 6-1. 반환받은 값이 1인 경우 - 방 생성 후 방장 입장
            if ( rpEntranceMetaRoom == 1 ) {
                // 6-1-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
                session.setAttribute(rpNickImage.getNickname(), idx);
                // 6-1-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                Meta.rpEntrance rpMasterEntrance = metaService.reEntrance(idx);
                // 6-1-3. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(idx);
                // 6-1-4. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                model.addAttribute("nickImage", rpNickImage);
                // 6-1-5. 6-1-2에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                model.addAttribute("metaRoom", rpMasterEntrance);
                // 6-1-6. 6-1-3에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                model.addAttribute("participantList", rpMetaRoomIdxList);
                // 6-1-7. 스터디룸 페이지로 이동한다.
                return "Meta/StudyRoom";
            // 6-2. 반환받은 값이 0인 경우 - 유저 첫 입장
            } else {
                // 7. 1에서 파라미터로 받아온 방 번호와 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
                Meta.rpEntrance rpNewEntrance = metaService.newEntrance(idx, rpNickImage);
                // 8. 7에서 반환받은 DTO가 존재하는지 체크한다.
                // 8-1. 반환받은 DTO가 존재하지 않는 경우 - 입장하는 방 존재 X
                if ( rpNewEntrance == null ) {
                    // 8-1-1. 에러메시지를 바인딩한다.
                    model.addAttribute("err", "해당 방의 정보가 없습니다.");
                    // 8-1-2. 메타 메인 페이지로 리다이렉트한다.
                    return "redirect:/meta";
                // 8-2. 반환받은 DTO가 존재하는 경우 - 입장하는 방 존재 O
                } else {
                    // 9. 7에서 반환받은 DTO 값 중 idx가 0인지 체크한다.
                    // 9-1. idx가 0인 경우 - 입장하는 방 정원초과
                    if ( rpNewEntrance.getIdx() == 0 ) {
                        // 9-1-1. 7에서 반환받은 DTO 값 중 metaTitle에 저장되있는 에러메시지를 바인딩한다.
                        model.addAttribute("err", rpNewEntrance.getMetaTitle());
                        // 9-1-2. 메타 메인 페이지로 리다이렉트한다.
                        return "redirect:/meta";
                    // 9-2. idx가 0이 아닌 경우 - 입장하는 방 첫 입장
                    } else {
                        // 9-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
                        //        첫 입장은 세션 값을 바인딩하지 않고, 재입장부터 세션 값을 바인딩해서 첫 입장과 재입장을 구분 짓는다.
                        session.setAttribute(rpNickImage.getNickname(), idx);
                        // 9-2-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                        List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(idx);
                        // 9-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                        model.addAttribute("nickImage", rpNickImage);
                        // 9-2-4. 7에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                        model.addAttribute("metaRoom", rpNewEntrance);
                        // 9-2-5. 9-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                        model.addAttribute("participantList", rpMetaRoomIdxList);
                        // 9-2-6. 스터디룸 페이지로 이동한다.
                        return "Meta/StudyRoom";
                    }
                }
            }
        }
    }

    // 카페룸 페이지
    @GetMapping("/caferoom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
    public String cafeRoom(@RequestParam long idx, Principal principal, Model model) { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 세션을 사용하기 위하여 HttpServletRequest을 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 4. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값이 존재하는지 체크한다.
        // 4-1. 세션에 값이 존재하는 경우 - 재입장(새로고침)
        if ( session.getAttribute(rpNickImage.getNickname()) != null ) {
            // 4-1-1. 2에서 반환받은 DTO 값 중 닉네임을 서비스에 전달한다.
            int entranceCheck = metaService.entranceCheck(rpNickImage.getNickname());
            // 4-1-1-1. 4-1-1에서 반환받은 값이 어떤것인지 체크한다.
            // 4-1-1-1-1. 반환받은 값이 0인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 X - 퇴장 후 첫 입장
            //            세션은 존재하고 방 내부 참여자 명단에는 존재하지 않다는 것은 퇴장 후 첫 입장을 의미한다.
            if ( entranceCheck == 0 ) {
                // 4-1-1-1-1-1. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                session.removeAttribute(rpNickImage.getNickname());
                // 4-1-1-1-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 스터리룸 페이지로 리다이렉트한다.
                return "redirect:/meta/caferoom?idx=" + idx;
            }
            // 4-1-1-1-2. 반환받은 값이 1인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 O - 재입장(새로고침)
            //            세션도 존재하고 방 내부 참여자 명단에도 존재한다는 것은 재입장(새로고침)을 의미한다.
            // 4-1-1-1-2-1. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
            Meta.rpEntrance rpReEntrance = metaService.reEntrance(idx);
            // 4-1-1-1-2-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
            List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(idx);
            // 4-1-1-1-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
            model.addAttribute("nickImage", rpNickImage);
            // 4-1-1-1-2-4. 4-1-1-1-2-1에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
            model.addAttribute("metaRoom", rpReEntrance);
            // 4-1-1-1-2-5. 4-1-1-1-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
            model.addAttribute("participantList", rpMetaRoomIdxList);
            // 4-1-1-1-2-6. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값을 바인딩한다.
            model.addAttribute("entryCheck", session.getAttribute(rpNickImage.getNickname()));
            // 4-1-1-1-2-7. 카페룸 페이지로 이동한다.
            return "Meta/CafeRoom";
        // 4-2. 세션에 값이 존재하지 않는 경우 - 첫 입장
        } else {
            // 5. 1에서 파라미터로 받아온 방 번호와 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 서비스에 전달한다.
            int rpEntranceMetaRoom = metaService.entranceMetaRoom(idx, rpNickImage.getNickname());
            // 6. 5에서 반환받은 값이 어떤것인지 체크한다.
            // 6-1. 반환받은 값이 1인 경우 - 방 생성 후 방장 입장
            if ( rpEntranceMetaRoom == 1 ) {
                // 6-1-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
                session.setAttribute(rpNickImage.getNickname(), idx);
                // 6-1-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                Meta.rpEntrance rpMasterEntrance = metaService.reEntrance(idx);
                // 6-1-3. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(idx);
                // 6-1-4. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                model.addAttribute("nickImage", rpNickImage);
                // 6-1-5. 6-1-2에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                model.addAttribute("metaRoom", rpMasterEntrance);
                // 6-1-6. 6-1-3에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                model.addAttribute("participantList", rpMetaRoomIdxList);
                // 6-1-7. 카페룸 페이지로 이동한다.
                return "Meta/CafeRoom";
            // 6-2. 반환받은 값이 0인 경우 - 유저 첫 입장
            } else {
                // 7. 1에서 파라미터로 받아온 방 번호와 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
                Meta.rpEntrance rpNewEntrance = metaService.newEntrance(idx, rpNickImage);
                // 8. 7에서 반환받은 DTO가 존재하는지 체크한다.
                // 8-1. 반환받은 DTO가 존재하지 않는 경우 - 입장하는 방 존재 X
                if ( rpNewEntrance == null ) {
                    // 8-1-1. 에러메시지를 바인딩한다.
                    model.addAttribute("err", "해당 방의 정보가 없습니다.");
                    // 8-1-2. 메타 메인 페이지로 리다이렉트한다.
                    return "redirect:/meta";
                // 8-2. 반환받은 DTO가 존재하는 경우 - 입장하는 방 존재 O
                } else {
                    // 9. 7에서 반환받은 DTO 값 중 idx가 0인지 체크한다.
                    // 9-1. idx가 0인 경우 - 입장하는 방 정원초과
                    if ( rpNewEntrance.getIdx() == 0 ) {
                        // 9-1-1. 7에서 반환받은 DTO 값 중 metaTitle에 저장되있는 에러메시지를 바인딩한다.
                        model.addAttribute("err", rpNewEntrance.getMetaTitle());
                        // 9-1-2. 메타 메인 페이지로 리다이렉트한다.
                        return "redirect:/meta";
                    // 9-2. idx가 0이 아닌 경우 - 입장하는 방 첫 입장
                    } else {
                        // 9-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
                        //        첫 입장은 세션 값을 바인딩하지 않고, 재입장부터 세션 값을 바인딩해서 첫 입장과 재입장을 구분 짓는다.
                        session.setAttribute(rpNickImage.getNickname(), idx);
                        // 9-2-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                        List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(idx);
                        // 9-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                        model.addAttribute("nickImage", rpNickImage);
                        // 9-2-4. 7에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                        model.addAttribute("metaRoom", rpNewEntrance);
                        // 9-2-5. 9-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                        model.addAttribute("participantList", rpMetaRoomIdxList);
                        // 9-2-6. 카페룸 페이지로 이동한다.
                        return "Meta/CafeRoom";
                    }
                }
            }
        }
    }

    // 자습실 페이지
    @GetMapping("/oneroom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
    public String oneRoom(@RequestParam long idx, Principal principal, Model model) throws InterruptedException { // 1. 파라미터로 입장한 방 번호를 받아온다.
        Thread.sleep(100);
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 세션을 사용하기 위하여 HttpServletRequest을 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 4. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값이 존재하는지 체크한다.
        // 4-1. 세션에 값이 존재하는 경우 - 재입장(새로고침)
        if ( session.getAttribute(rpNickImage.getNickname()) != null ) {
            int entranceCheckOneRoom = metaService.entranceCheckOneRoom(idx);
            if ( entranceCheckOneRoom == 0 ) {
                metaService.entranceOneRoom(idx);
                // 4-1-1-1-1-1. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                session.removeAttribute(rpNickImage.getNickname());
                // 4-1-1-1-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 스터리룸 페이지로 리다이렉트한다.
                return "redirect:/meta/oneroom?idx=" + idx;
            }
            // 4-1-1. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
            Meta.rpEntrance rpReEntrance = metaService.reEntrance(idx);
            // 4-1-2. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
            model.addAttribute("nickImage", rpNickImage);
            // 4-1-3. 4-1-1에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
            model.addAttribute("metaRoom", rpReEntrance);
            // 4-1-4. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값을 바인딩한다.
            model.addAttribute("entryCheck", session.getAttribute(rpNickImage.getNickname()));
            // 4-1-5. 자습실 페이지로 이동한다.
            return "Meta/OneRoom";
        // 4-2. 세션에 값이 존재하는 경우 - 재입장(새로고침)
        } else {
            // 4-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
            session.setAttribute(rpNickImage.getNickname(), idx);
            // 4-2-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
            Meta.rpEntrance rpMasterEntrance = metaService.reEntrance(idx);
            // 4-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
            model.addAttribute("nickImage", rpNickImage);
            // 4-2-4. 4-2-2에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
            model.addAttribute("metaRoom", rpMasterEntrance);
            // 4-2-5. 자습실 페이지로 이동한다.
            return "Meta/OneRoom";
        }
    }

    // 방장 위임
    @GetMapping("delegatemaster")
    @ResponseBody // 비동기 통신 fetch를 사용하였기에 필요한 어노테이션
    public int delegateMaster(@RequestParam long idx, @RequestParam String nickname) { // 1. 파라미터로 입장한 방 번호와 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호와 닉네임을 서비스에 전달한다.
        int res = metaService.delegateMaster(idx, nickname);
        // 3. 2에서 반환받은 방장 위임 결과 값을 클라이언트로 반환한다.
        return res;
    }

    // 타인에 의한 방 퇴장 fetch (방 내부 참여자 명단에서 삭제된다.)
    @GetMapping("/exit")
    @ResponseBody // 비동기 통신 fetch를 사용하였기에 필요한 어노테이션
    public int exitRoom(@RequestParam long idx, @RequestParam String nickname) { // 1. 파라미터로 입장한 방 번호와 닉네임을 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호와 닉네임을 서비스에 전달한다.
        int res = metaService.exit(idx, nickname);
        // 3. 2에서 반환받은 방 퇴장 결과 값을 클라이언트로 반환한다.
        return res;
    }

    // 본인에 의한 방 퇴장 (방 내부 참여자 명단에서 삭제된다.)
    @GetMapping("/selfexit")
    public String btnExitRoom(@RequestParam long idx, @RequestParam String nickname) { // 1. 파라미터로 입장한 방 번호와 닉네임을 받아온다.
        // 2. 세션을 사용하기 위하여 HttpServletRequest을 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 3. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
        session.removeAttribute(nickname);
        // 4. 1에서 파라미터로 받아온 방 번호와 닉네임을 서비스에 전달하다.
        metaService.exit(idx, nickname);
        // 5. 메타 메인 페이지로 리다이렉트한다.
        return "redirect:/meta";
    }

    @GetMapping("/oneroomexit")
    @ResponseBody
    public void exitOneRoom(@RequestParam long idx, @RequestParam String nickname) { // 1. 파라미터로 입장한 방 번호와 닉네임을 받아온다.
        try {
            System.out.println(1);
            metaService.exitOneRoom(idx);
            Thread.sleep(1000);
            int metaRecruitingPersonnel = metaService.entranceCheckOneRoom(idx);
            if ( metaRecruitingPersonnel == 1 ) {
                System.out.println(2);
            } else {
                System.out.println(3);
                // 2. 세션을 사용하기 위하여 HttpServletRequest을 통해 세션 객체를 가져온다.
                HttpSession session = request.getSession();
                // 3. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                session.removeAttribute(nickname);
                // 4. 1에서 파라미터로 받아온 방 번호와 닉네임을 서비스에 전달하다.
                metaService.deleteOneRoom(idx);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 방 삭제 (방이 삭제된다.)
    @GetMapping("/delete")
    public String deleteRoom(@RequestParam long idx, @RequestParam String nickname) { // 1. 파라미터로 입장한 방 번호와 닉네임을 받아온다.
        // 2. 세션을 사용하기 위하여 HttpServletRequest을 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 3. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
        session.removeAttribute(nickname);
        // 4. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
        metaService.delete(idx);
        // 5. 메타 메인 페이지로 리다이렉트한다.
        return "redirect:/meta";
    }
}