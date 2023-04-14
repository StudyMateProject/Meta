package com.study.mate.controller;

import com.study.mate.entity.Alarm;
import com.study.mate.entity.Member;
import com.study.mate.entity.Meta;
import com.study.mate.entity.MetaRoom;
import com.study.mate.service.MetaService;
import com.study.mate.service.MyPageService;
import com.study.mate.service.SignUpService;
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
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/meta")
public class MetaController {
    // 메타 서비스
    @Autowired
    MetaService metaService;
    // 회원가입 및 로그인 인증 서비스
    @Autowired
    SignUpService signUpService;
    // 마이 페이지 서비스
    @Autowired
    MyPageService myPageService;

    // HTTP 요청에 대한 정보를 담고 있는 클래스
    @Autowired
    HttpServletRequest request;

    // STOMP Socket에서 메시지를 보낼 때 퇴장 메시지와 재입장(새로고침) 메시지를 구분하기 위한 재입장(새로고침) 체크용 Map
    Map<String, Long> reEnterCheck = new HashMap<>();

    // 방장 혼자일때 퇴장하는 경우 방 삭제 메소드
    public void deleteRoomMaster(@RequestParam long idx) { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
        metaService.delete(idx);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 메타 메인 페이지
    @GetMapping("")
    public String meta(@RequestParam(value = "errRoom", required = false) String errRoom, // 1-1. URL 파라미터를 통해 넘어오는 방 에러 메세지가 있는 경우 받아온다.
                       @RequestParam(value = "idx", required = false) Long idx, // 1-2. URL 파라미터를 통해 넘어오는 방 번호가 있는 경우 받아온다.
                       @RequestParam(value = "nickname", required = false) String nickname, // 1-3. URL 파라미터를 통해 넘어오는 닉네임이 있는 경우 받아온다.
                       @RequestParam(value = "err", required = false) String err, // 1-4. URL 파라미터를 통해 넘어오는 에러 메시지가 있는 경우 받아온다.
                       Principal principal, Model model) { // 1. URL 파라미터를 통해 넘어오는 각종 값들을 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpMetaProfile rpMetaProfile = signUpService.metaProfile(principal.getName());
        // 3. 서비스를 통해 현재 생성된 메타 방을 모두 조회해서, List 형태의 DTO로 반환 받아온다.
        List<Meta.rpMetaList> rpMetaList = metaService.metaList();
        // 4. 3에서 반환받은 List 형태의 DTO를 방의 타입별로 나누기 위해 각 방의 타입마다 List 형태의 DTO를 생성한다.
        // 4-1. 방 타입이 스터디일 경우 - studyRoom List DTO
        List<Meta.rpMetaList> studyList = new ArrayList<>();
        // 4-2. 방 타입이 카페일 경우 - cafeRoom List DTO
        List<Meta.rpMetaList> cafeList = new ArrayList<>();
        // 5. foreach문을 사용하여 3에서 반환받은 List 형태의 DTO에 들어있는 값들을 하나씩 가져온다.
        for ( Meta.rpMetaList metaList : rpMetaList ) {
            // 6. 5에서 가져온 값 중 방 타입을 가져와 체크한다.
            // 6-1. 방 타입이 스터디일 경우
            if ( metaList.getMetaType().equals("studyRoom") ) {
                // 6-1-1. 4-1에서 생성한 studyRoom List DTO에 추가한다.
                studyList.add(metaList);
            // 6-2. 방 타입이 카페일 경우
            } else if ( metaList.getMetaType().equals("cafeRoom") ) {
                // 6-2-1. 4-2에서 생성한 cafeRoom List DTO에 추가한다.
                cafeList.add(metaList);
            }
        }

        // 7. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        List<Alarm> alarmList = myPageService.findEmailId(principal.getName());

        // 8. 5에서 foreach문을 사용하여 방 타입별로 값을 새로 추가해 만든 List 형태의 DTO들을 바인딩한다.
        // 8-1. 4-1에서 생성하고 6-1에서 값을 추가한 studyRoom List DTO를 바인딩한다.
        model.addAttribute("studyList", studyList);
        // 8-2. 4-2에서 생성하고 6-2에서 값을 추가한 cafeRoom List DTO를 바인딩한다.
        model.addAttribute("cafeList", cafeList);
        // 9. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
        model.addAttribute("metaProfile", rpMetaProfile);
        // 10. 7에서 반환받은 알람 List를 바인딩한다.
        model.addAttribute("alarmList", alarmList);
        // 11. 검색 체크값을 바인딩한다. - 0 : 검색 안했을 경우
        model.addAttribute("check", 0);
        // 12. 검색에 사용할 DTO를 바인딩한다.
        model.addAttribute("metaDTO", new Meta.rqSearchMetaList());
        // 13. 1에서 파라미터로 받아온 방 에러 메시지가 존재하는지 체크한다.
        // 13-1. 방 에러 메시지가 존재하는 경우
        if ( errRoom != null ) {
            // 13-1-1. 1에서 파라미터로 받아온 방 에러 메시지를 바인딩한다.
            model.addAttribute("errRoom", errRoom);
            // 13-1-2. 1에서 파라미터로 받아온 방 번호를 바인딩한다.
            model.addAttribute("idx", idx);
            // 13-1-3. 1에서 파라미터로 받아온 닉네임을 바인딩한다.
            model.addAttribute("nickname", nickname);
        }
        // 14. 1에서 파라미터로 받아온 에러 메시지가 존재하는지 체크한다.
        // 14-1. 에러 메시지가 존재하는 경우
        if ( err != null ) {
            // 14-1-1. 1에서 파라미터로 받아온 에러 메시지를 바인딩한다.
            model.addAttribute("err", err);
        }
        // 15. 메타 메인 페이지로 이동한다.
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
            }
        }

        // 7. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        List<Alarm> alarmList = myPageService.findEmailId(principal.getName());

        // 8. 5에서 foreach문을 사용하여 방 타입별로 값을 새로 추가해 만든 List 형태의 DTO들을 바인딩한다.
        // 8-1. 4-1에서 생성하고 6-1에서 값을 추가한 studyRoom List DTO를 바인딩한다.
        model.addAttribute("studyList", studyList);
        // 8-2. 4-2에서 생성하고 6-2에서 값을 추가한 cafeRoom List DTO를 바인딩한다.
        model.addAttribute("cafeList", cafeList);
        // 9. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
        model.addAttribute("metaProfile", rpMetaProfile);
        // 10. 7에서 반환받은 알람 List를 바인딩한다.
        model.addAttribute("alarmList", alarmList);
        // 11. 검색 체크값을 바인딩한다. - 1 : 검색 했을 경우
        model.addAttribute("check", 1);
        // 12. 검색에 사용할 DTO를 바인딩한다.
        model.addAttribute("metaDTO", new Meta.rqSearchMetaList());
        // 13. 메타 메인 페이지로 이동한다.
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
    public String createMetaRoom(Meta.rqCreateMeta rqCreateMeta, Principal principal) { // 1. 파라미터로 form에서 넘어온 방 생성 DTO를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 1에서 파라미터로 받아온 방 생성 DTO와 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
        Meta.rpCreateMeta rpCreateMeta = metaService.createRoom(rqCreateMeta, rpNickImage);
        // 4. 세션을 사용하기 위하여 위에서 @Autowired로 생성한 HttpServletRequest를 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 5. 3에서 반환받은 DTO 값 중 방 타입을 체크한다.
        // 5-1. 방 타입이 스터디일 경우
        if ( rpCreateMeta.getMetaType().equals("studyRoom") ) {
            // 5-1-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용함과 동시에 값으로 사용하여 세션에 추가한다. - 방장 체크 값
            session.setAttribute(rpNickImage.getNickname(), rpNickImage.getNickname());
            // 5-1-2. 3에서 반환받은 DTO 값 중 방 번호를 가지고 스터디 페이지로 리다이렉트한다.
            return "redirect:/meta/studyroom?idx=" + rpCreateMeta.getIdx();
        // 5-2. 방 타입이 카페일 경우
        } else if ( rpCreateMeta.getMetaType().equals("cafeRoom") ) {
            // 5-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용함과 동시에 값으로 사용하여 세션에 추가한다. - 방장 체크 값
            session.setAttribute(rpNickImage.getNickname(), rpNickImage.getNickname());
            // 5-2-2. 3에서 반환받은 DTO 값 중 방 번호를 가지고 카페 페이지로 리다이렉트한다.
            return "redirect:/meta/caferoom?idx=" + rpCreateMeta.getIdx();
        // 5-3. 방 타입이 자습일 경우
        } else {
            // 5-3-1. 3에서 반환받은 DTO 값 중 방 번호를 가지고 자습실 페이지로 리다이렉트한다.
            return "redirect:/meta/oneroom?idx=" + rpCreateMeta.getIdx();
        }
    }

    // 스터디룸 입장
    @GetMapping("/studyroom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
    public String studyRoom(@RequestParam long idx, Principal principal, Model model) throws UnsupportedEncodingException { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 세션을 사용하기 위하여 위에서 @Autowired로 생성한 HttpServletRequest를 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 4. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값이 존재하는지 체크한다.
        // 4-1. 세션에 값이 존재하는 경우 - 재입장(새로고침)
        if ( session.getAttribute(rpNickImage.getNickname()) != null ) {
            // 5. 세션에 존재하는 값이 닉네임인지 방 번호인지 체크한다.
            // 5-1. 세션에 값이 닉네임인 경우 - 방 생성 후 방장 첫 입장
            if ( session.getAttribute(rpNickImage.getNickname()).equals(rpNickImage.getNickname()) ) {
                // 5-1-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션을 변경한다.
                session.setAttribute(rpNickImage.getNickname(), idx);
                // 5-1-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                Meta.rpEntrance rpMasterEntrance = metaService.reEntrance(idx);
                // 5-1-3. 첫 번째 인자는 5-1-2에서 반환받은 입장한 방 정보 DTO를,
                //        두 번째 인자는 재입장이 아닌 첫 입장이므로 null 값으로 설정하여 서비스에 전달한다.
                List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(rpMasterEntrance, null);
                // 5-1-4. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                model.addAttribute("nickImage", rpNickImage);
                // 5-1-5. 5-1-2에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                model.addAttribute("metaRoom", rpMasterEntrance);
                // 5-1-6. 5-1-3에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                model.addAttribute("participantList", rpMetaRoomIdxList);
                // 5-1-7. 스터디룸 페이지로 이동한다.
                return "Meta/StudyRoom";
            // 5-2. 세션에 값이 방 번호인 경우
            } else {
                // 6. 세션에 존재하는 방 번호가 1에서 파라미터로 받아온 방 번호와 같은지 체크한다.
                // 6-1. 세션에 값이 방 번호와 다른 경우 - 방장 혼자일때 방 퇴장 및 삭제 이후 새로운 방 생성 및 첫 입장
                //      방장 혼자일때 방을 퇴장하는 경우 방은 삭제되지만 세션은 제거되지 못하고 남아있기에 세션에 값은 이전 퇴장한 방 번호가 남아있다.
                if ( (long) session.getAttribute(rpNickImage.getNickname()) != idx ) {
                    // 6-1-1. 3에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                    session.removeAttribute(rpNickImage.getNickname());
                    // 6-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 스터리룸 페이지로 리다이렉트한다.
                    return "redirect:/meta/studyroom?idx=" + idx;
                // 6-2. 세션에 값이 방 번호와 같은 경우 - 재입장(새로고침)
                } else {
                    // 7. 2에서 반환받은 DTO 값 중 닉네임을 서비스에 전달한다.
                    int entranceCheck = metaService.entranceCheck(rpNickImage.getNickname());
                    // 8. 7에서 반환받은 값이 어떤것인지 체크한다.
                    // 8-1. 반환받은 값이 0인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 X - 퇴장 후 첫 입장
                    //      세션은 존재하고 방 내부 참여자 명단에는 존재하지 않는다는 것은, 방장에 의한 퇴장 후 첫 입장을 의미한다.
                    //      본인이 나가기 버튼을 사용해 퇴장한 것이 아닌, 뒤로가기 및 페이지 닫기 등 예외적인 방법으로 페이지를 벗어나 퇴장하는 것으로,
                    //      이때는 방장이 퇴장 처리를 하기에 방 내부 참여자 명단에서는 제거되지만, 세션은 제거되지 못하고 남아있다.
                    if ( entranceCheck == 0 ) {
                        // 8-1-1. 3에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                        session.removeAttribute(rpNickImage.getNickname());
                        // 8-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 스터리룸 페이지로 리다이렉트한다.
                        return "redirect:/meta/studyroom?idx=" + idx;
                    // 8-2. 반환받은 값이 1인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 O - 재입장(새로고침)
                    //      세션도 존재하고 방 내부 참여자 명단에도 존재한다는 것은 재입장(새로고침)을 의미한다.
                    } else {
                        // 9. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                        Meta.rpEntrance rpReEntrance = metaService.reEntrance(idx);
                        // 10. 9에서 반환받은 입장한 방 정보 DTO가 존재하는지 체크한다.
                        // 10-1. 입장한 방 정보 DTO가 존재하지 않는 경우
                        if ( rpReEntrance == null ) {
                            // 10-1-1. 에러값을 바인딩한다.
                            model.addAttribute("err", "1");
                        // 10-2. 입장한 방 정보 DTO가 존재하는 경우
                        } else {
                            // 10-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여, 위에서 생성한 재입장(새로고침) 체크용 Map에 추가한다.
                            reEnterCheck.put(rpNickImage.getNickname(), idx);
                            // 10-2-2. 첫 번째 인자는 9에서 반환받은 입장한 방 정보 DTO를,
                            //         두 번째 인자는 첫 입장이 아닌 재입장이므로 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
                            List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(rpReEntrance, rpNickImage);
                            // 10-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                            model.addAttribute("nickImage", rpNickImage);
                            // 10-2-4. 9에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                            model.addAttribute("metaRoom", rpReEntrance);
                            // 10-2-5. 10-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                            model.addAttribute("participantList", rpMetaRoomIdxList);
                            // 10-2-6. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값을 바인딩한다.
                            model.addAttribute("entryCheck", session.getAttribute(rpNickImage.getNickname()));
                        } // rpReEntrance == null
                        // 11. 스터디룸 페이지로 이동한다.
                        return "Meta/StudyRoom";
                    } // entranceCheck == 0
                } // (long) session.getAttribute(rpNickImage.getNickname()) != idx
            } // session.getAttribute(rpNickImage.getNickname()).equals(rpNickImage.getNickname())
        // 4-2. 세션에 값이 존재하지 않는 경우 - 첫 입장
        } else {
            // 5. 1에서 파라미터로 받아온 방 번호와 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
            Meta.rpEntrance rpNewEntrance = metaService.newEntrance(idx, rpNickImage);
            // 6. 5에서 반환받은 DTO가 존재하는지 체크한다.
            // 6-1. 반환받은 DTO가 존재하지 않는 경우 - 입장하는 방 존재 X
            if ( rpNewEntrance == null ) {
                // 6-1-1. URL 파라미터에 에러 메시지를 가지고 메타 메인 페이지로 리다이렉트한다.
                return "redirect:/meta?err=" + URLEncoder.encode("해당 방의 정보가 없습니다.", "UTF-8");
            // 6-2. 반환받은 DTO가 존재하는 경우 - 입장하는 방 존재 O
            } else {
                // 7. 5에서 반환받은 DTO 값 중 idx가 0인지 체크한다.
                // 7-1. idx가 0인 경우 - 입장하는 방 정원초과
                if ( rpNewEntrance.getIdx() == 0 ) {
                    // 7-1-1. 5에서 반환받은 DTO 값 중 metaTitle에 저장되있는 에러메시지를 바인딩한다.
                    model.addAttribute("err", rpNewEntrance.getMetaTitle());
                    // 7-1-2. 메타 메인 페이지로 리다이렉트한다.
                    return "redirect:/meta";
                // 7-2. idx가 0이 아닌 경우 - 입장하는 방 첫 입장
                } else {
                    // 7-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
                    //        첫 입장은 세션 값을 바인딩하지 않고, 재입장(새로고침)부터 세션 값을 바인딩해서 첫 입장과 재입장을 구분 짓는다.
                    session.setAttribute(rpNickImage.getNickname(), idx);
                    // 7-2-2. 첫 번째 인자는 5에서 반환받은 입장한 방 정보 DTO를,
                    //        두 번째 인자는 재입장이 아닌 첫 입장이므로 null 값으로 설정하여 서비스에 전달한다.
                    List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(rpNewEntrance, null);
                    // 7-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                    model.addAttribute("nickImage", rpNickImage);
                    // 7-2-4. 5에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                    model.addAttribute("metaRoom", rpNewEntrance);
                    // 7-2-5. 7-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                    model.addAttribute("participantList", rpMetaRoomIdxList);
                    // 7-2-6. 스터디룸 페이지로 이동한다.
                    return "Meta/StudyRoom";
                } // rpNewEntrance.getIdx() == 0
            } // rpNewEntrance == null
        } // session.getAttribute(rpNickImage.getNickname()) != null
    } // studyRoom(@RequestParam long idx, Principal principal, Model model)

    // 카페룸 페이지
    @GetMapping("/caferoom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
    public String cafeRoom(@RequestParam long idx, Principal principal, Model model) throws UnsupportedEncodingException { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 세션을 사용하기 위하여 위에서 @Autowired로 생성한 HttpServletRequest를 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 4. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값이 존재하는지 체크한다.
        // 4-1. 세션에 값이 존재하는 경우 - 재입장(새로고침)
        if ( session.getAttribute(rpNickImage.getNickname()) != null ) {
            // 5. 세션에 존재하는 값이 닉네임인지 방 번호인지 체크한다.
            // 5-1. 세션에 값이 닉네임인 경우 - 방 생성 후 방장 첫 입장
            if ( session.getAttribute(rpNickImage.getNickname()).equals(rpNickImage.getNickname()) ) {
                // 5-1-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션을 변경한다.
                session.setAttribute(rpNickImage.getNickname(), idx);
                // 5-1-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                Meta.rpEntrance rpMasterEntrance = metaService.reEntrance(idx);
                // 5-1-3. 첫 번째 인자는 5-1-2에서 반환받은 입장한 방 정보 DTO를,
                //        두 번째 인자는 재입장이 아닌 첫 입장이므로 null 값으로 설정하여 서비스에 전달한다.
                List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(rpMasterEntrance, null);
                // 5-1-4. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                model.addAttribute("nickImage", rpNickImage);
                // 5-1-5. 5-1-2에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                model.addAttribute("metaRoom", rpMasterEntrance);
                // 5-1-6. 5-1-3에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                model.addAttribute("participantList", rpMetaRoomIdxList);
                // 5-1-7. 스터디룸 페이지로 이동한다.
                return "Meta/CafeRoom";
            // 5-2. 세션에 값이 방 번호인 경우 - 재입장(새로고침)
            } else {
                // 6. 세션에 존재하는 방 번호가 1에서 파라미터로 받아온 방 번호와 같은지 체크한다.
                // 6-1. 세션에 값이 방 번호와 다른 경우 - 방장 혼자일때 방 퇴장 및 삭제 이후 새로운 방 생성 및 첫 입장
                //      방장 혼자일때 방을 퇴장하는 경우 방은 삭제되지만 세션은 제거되지 못하고 남아있기에 세션에 값은 이전 퇴장한 방 번호가 남아있다.
                if ( (long) session.getAttribute(rpNickImage.getNickname()) != idx ) {
                    // 6-1-1. 3에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                    session.removeAttribute(rpNickImage.getNickname());
                    // 6-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 카페룸 페이지로 리다이렉트한다.
                    return "redirect:/meta/caferoom?idx=" + idx;
                // 6-2. 세션에 값이 방 번호와 같은 경우 - 재입장(새로고침)
                } else {
                    // 7. 2에서 반환받은 DTO 값 중 닉네임을 서비스에 전달한다.
                    int entranceCheck = metaService.entranceCheck(rpNickImage.getNickname());
                    // 8. 7에서 반환받은 값이 어떤것인지 체크한다.
                    // 8-1. 반환받은 값이 0인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 X - 퇴장 후 첫 입장
                    //      세션은 존재하고 방 내부 참여자 명단에는 존재하지 않는다는 것은, 방장에 의한 퇴장 후 첫 입장을 의미한다.
                    //      본인이 나가기 버튼을 사용해 퇴장한 것이 아닌, 뒤로가기 및 페이지 닫기 등 예외적인 방법으로 페이지를 벗어나 퇴장하는 것으로,
                    //      이때는 방장이 퇴장 처리를 하기에 방 내부 참여자 명단에서는 제거되지만, 세션은 제거되지 못하고 남아있다.
                    if ( entranceCheck == 0 ) {
                        // 8-1-1. 3에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                        session.removeAttribute(rpNickImage.getNickname());
                        // 8-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 카페룸 페이지로 리다이렉트한다.
                        return "redirect:/meta/caferoom?idx=" + idx;
                    // 8-2. 반환받은 값이 1인 경우 - 세션 존재 O / 방 내부 참여자 명단 존재 O - 재입장(새로고침)
                    //      세션도 존재하고 방 내부 참여자 명단에도 존재한다는 것은 재입장(새로고침)을 의미한다.
                    } else {
                        // 9. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                        Meta.rpEntrance rpReEntrance = metaService.reEntrance(idx);
                        // 10. 9에서 반환받은 입장한 방 정보 DTO가 존재하는지 체크한다.
                        // 10-1. 입장한 방 정보 DTO가 존재하지 않는 경우
                        if ( rpReEntrance == null ) {
                            // 10-1-1. 에러값을 바인딩한다.
                            model.addAttribute("err", "1");
                        // 10-2. 입장한 방 정보 DTO가 존재하는 경우
                        } else {
                            // 10-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여, 위에서 생성한 재입장(새로고침) 체크용 Map에 추가한다.
                            reEnterCheck.put(rpNickImage.getNickname(), idx);
                            // 10-2-2. 첫 번째 인자는 9에서 반환받은 입장한 방 정보 DTO를,
                            //         두 번째 인자는 첫 입장이 아닌 재입장이므로 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
                            List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(rpReEntrance, rpNickImage);
                            // 10-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                            model.addAttribute("nickImage", rpNickImage);
                            // 10-2-4. 9에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                            model.addAttribute("metaRoom", rpReEntrance);
                            // 10-2-5. 10-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                            model.addAttribute("participantList", rpMetaRoomIdxList);
                            // 10-2-6. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값을 바인딩한다.
                            model.addAttribute("entryCheck", session.getAttribute(rpNickImage.getNickname()));
                        } // rpReEntrance == null
                        // 11. 카페룸 페이지로 이동한다.
                        return "Meta/CafeRoom";
                    } // entranceCheck == 0
                } // (long) session.getAttribute(rpNickImage.getNickname()) != idx
            } // session.getAttribute(rpNickImage.getNickname()).equals(rpNickImage.getNickname())
        // 4-2. 세션에 값이 존재하지 않는 경우 - 첫 입장
        } else {
            // 5. 1에서 파라미터로 받아온 방 번호와 2에서 반환받은 로그인 유저 정보 DTO를 서비스에 전달한다.
            Meta.rpEntrance rpNewEntrance = metaService.newEntrance(idx, rpNickImage);
            // 6. 5에서 반환받은 DTO가 존재하는지 체크한다.
            // 6-1. 반환받은 DTO가 존재하지 않는 경우 - 입장하는 방 존재 X
            if ( rpNewEntrance == null ) {
                // 6-1-1. URL 파라미터에 에러 메시지를 가지고 메타 메인 페이지로 리다이렉트한다.
                return "redirect:/meta?err=" + URLEncoder.encode("해당 방의 정보가 없습니다.", "UTF-8");
            // 6-2. 반환받은 DTO가 존재하는 경우 - 입장하는 방 존재 O
            } else {
                // 7. 5에서 반환받은 DTO 값 중 idx가 0인지 체크한다.
                // 7-1. idx가 0인 경우 - 입장하는 방 정원초과
                if ( rpNewEntrance.getIdx() == 0 ) {
                    // 7-1-1. 5에서 반환받은 DTO 값 중 metaTitle에 저장되있는 에러메시지를 바인딩한다.
                    model.addAttribute("err", rpNewEntrance.getMetaTitle());
                    // 7-1-2. 메타 메인 페이지로 리다이렉트한다.
                    return "redirect:/meta";
                // 7-2. idx가 0이 아닌 경우 - 입장하는 방 첫 입장
                } else {
                    // 7-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
                    //        첫 입장은 세션 값을 바인딩하지 않고, 재입장(새로고침)부터 세션 값을 바인딩해서 첫 입장과 재입장을 구분 짓는다.
                    session.setAttribute(rpNickImage.getNickname(), idx);
                    // 7-2-2. 첫 번째 인자는 5에서 반환받은 입장한 방 정보 DTO를,
                    //        두 번째 인자는 재입장이 아닌 첫 입장이므로 null 값으로 설정하여 서비스에 전달한다.
                    List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(rpNewEntrance, null);
                    // 7-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                    model.addAttribute("nickImage", rpNickImage);
                    // 7-2-4. 5에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                    model.addAttribute("metaRoom", rpNewEntrance);
                    // 7-2-5. 7-2-2에서 반환받은 입장한 방 내부 참여자 명단 DTO를 바인딩한다.
                    model.addAttribute("participantList", rpMetaRoomIdxList);
                    // 7-2-6. 카페룸 페이지로 이동한다.
                    return "Meta/CafeRoom";
                } // rpNewEntrance.getIdx() == 0
            } // rpNewEntrance == null
        } // session.getAttribute(rpNickImage.getNickname()) != null
    } // cafeRoom(@RequestParam long idx, Principal principal, Model model)

    // 자습실 페이지
    @GetMapping("/oneroom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
    public String oneRoom(@RequestParam long idx, Principal principal, Model model) throws InterruptedException { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 전달한다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 세션을 사용하기 위하여 위에서 @Autowired로 생성한 HttpServletRequest를 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 4. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값이 존재하는지 체크한다.
        // 4-1. 세션에 값이 존재하는 경우 - 재입장(새로고침)
        if ( session.getAttribute(rpNickImage.getNickname()) != null ) {
            // 5. 세션에 존재하는 값이 1에서 파라미터로 받아온 방 번호와 같은지 체크한다.
            // 5-1. 세션에 값이 방 번호와 다른 경우 - 방장 혼자일때 방 퇴장 및 삭제 이후 새로운 방 생성 및 첫 입장
            //      방장 혼자일때 방을 퇴장하는 경우 방은 삭제되지만 세션은 제거되지 못하고 남아있기에 세션에 값은 이전 퇴장한 방 번호가 남아있다.
            if ( (long) session.getAttribute(rpNickImage.getNickname()) != idx ) {
                // 5-1-1. 3에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
                session.removeAttribute(rpNickImage.getNickname());
                // 5-1-2. 1에서 파라미터로 받아온 방 번호를 가지고 자습실 페이지로 리다이렉트한다.
                return "redirect:/meta/oneroom?idx=" + idx;
            // 5-2. 세션에 값이 방 번호와 같은 경우 - 재입장(새로고침)
            } else {
                // 5-2-1. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
                Meta.rpEntrance rpReEntrance = metaService.reEntrance(idx);
                // 5-2-2. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
                model.addAttribute("nickImage", rpNickImage);
                // 5-2-3. 3에서 가져온 세션 객체를 통해 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임 키에 해당하는 세션에 값을 바인딩한다.
                model.addAttribute("entryCheck", session.getAttribute(rpNickImage.getNickname()));
                // 5-2-4. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여, 위에서 생성한 재입장(새로고침) 체크용 Map에 추가한다.
                reEnterCheck.put(rpNickImage.getNickname(), idx);
                // 5-2-5. 5-2-1에서 반환받은 입장한 방 정보 DTO가 존재하는지 체크한다.
                // 5-2-5-1. 반환받은 입장한 방 정보 DTO가 존재하지 않는 경우
                if ( rpReEntrance == null ) {
                    // 5-2-5-1-1. 에러값을 바인딩한다.
                    model.addAttribute("err", "1");
                // 5-2-5-2. 반환받은 입장한 방 정보 DTO가 존재하는 경우
                } else {
                    // 5-2-5-2-1. 5-2-1에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
                    model.addAttribute("metaRoom", rpReEntrance);
                } // rpReEntrance == null
                // 5-2-6. 자습실 페이지로 이동한다.
                return "Meta/OneRoom";
            } // (long) session.getAttribute(rpNickImage.getNickname()) != idx
        // 4-2. 세션에 값이 존재하지 않는 경우 - 첫 입장
        } else {
            // 4-2-1. 2에서 반환받은 로그인 유저 정보 DTO 값 중 닉네임을 키로 사용하고, 1에서 받아온 방 번호를 값으로 사용하여 세션에 추가한다.
            //        첫 입장은 세션 값을 바인딩하지 않고, 재입장(새로고침)부터 세션 값을 바인딩해서 첫 입장과 재입장을 구분 짓는다.
            session.setAttribute(rpNickImage.getNickname(), idx);
            // 4-2-2. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
            Meta.rpEntrance rpMasterEntrance = metaService.reEntrance(idx);
            // 4-2-3. 2에서 반환받은 로그인 유저 정보 DTO를 바인딩한다.
            model.addAttribute("nickImage", rpNickImage);
            // 4-2-4. 4-2-2에서 반환받은 입장한 방 정보 DTO를 바인딩한다.
            model.addAttribute("metaRoom", rpMasterEntrance);
            // 4-2-5. 자습실 페이지로 이동한다.
            return "Meta/OneRoom";
        } // session.getAttribute(rpNickImage.getNickname()) != null
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
        // 2. 세션을 사용하기 위하여 위에서 @Autowired로 생성한 HttpServletRequest를 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 3. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
        session.removeAttribute(nickname);
        // 4. 1에서 파라미터로 받아온 방 번호와 닉네임을 서비스에 전달하다.
        metaService.exit(idx, nickname);
        // 5. 메타 메인 페이지로 리다이렉트한다.
        return "redirect:/meta";
    }

    // 방 삭제 (방이 삭제된다.)
    @GetMapping("/delete")
    public String deleteRoom(@RequestParam long idx, @RequestParam String nickname) { // 1. 파라미터로 입장한 방 번호와 닉네임을 받아온다.
        // 2. 세션을 사용하기 위하여 위에서 @Autowired로 생성한 HttpServletRequest를 통해 세션 객체를 가져온다.
        HttpSession session = request.getSession();
        // 3. 2에서 가져온 세션 객체를 통해 1에서 파라미터로 받아온 닉네임 키에 해당하는 세션을 제거한다.
        session.removeAttribute(nickname);
        // 4. 1에서 파라미터로 받아온 방 번호를 서비스에 전달한다.
        metaService.delete(idx);
        // 5. 메타 메인 페이지로 리다이렉트한다.
        return "redirect:/meta";
    }
}