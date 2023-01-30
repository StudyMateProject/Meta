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

import java.security.Principal;
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

    // 메타 메인 페이지
    @GetMapping("")
    public String meta(Model model) {
        // 1. 서비스를 통해 현재 생성된 메타 방을 모두 조회해 DTO로 가져온다.
        List<Meta.rpMetaList> metaList = metaService.metaList();
        // 5. 반환받은 List형태의 DTO를 바인딩한다.
        model.addAttribute("metaList", metaList);
        // 0. 검색에 사용할 DTO를 바인딩한다.
        model.addAttribute("metaDTO", new Meta.rqSearchMetaList());
        // 메타 메인 페이지로 이동
        return "Meta/MetaRoom";
    }

    // 방 만들기 페이지
    @GetMapping("/createmetaform")
    public String createMetaForm(Model model) {
        // 0. 방 만들기에 사용할 DTO를 바인딩한다.
        model.addAttribute("metaDTO", new Meta.rqCreateMeta());
        // 방만들기 페이지로 이동
        return "Meta/CreateMetaForm";
    }

    // 방 만들기
    @GetMapping("/createmetaform/createmeta")
    public String createMeta(Meta.rqCreateMeta rqCreateMeta) { // 1. DTO로 form값을 다 받아온다.
        // 2. 받아온 DTO를 서비스에 넘겨준다.
        metaService.createRoom(rqCreateMeta);
        // 메타 메인 페이지로 이동
        return "redirect:/meta";
    }

    // 방 이름 및 분야별로 검색
    @GetMapping("/search")
    public String searchMeta(Meta.rqSearchMetaList rqSearchMetaList, Model model) { // 1. DTO로 form값을 다 받아온다.
        // 2. 받아온 DTO를 서비스에 넘겨준다.
        List<Meta.rpSearchMetaList> rpSearchMetaList = metaService.searchMetaList(rqSearchMetaList);
        // 8. 반환받은 List형태의 DTO를 바인딩한다.
        model.addAttribute("searchMetaList", rpSearchMetaList);
        // 메타 메인 페이지로 이동
        return "Meta/MetaRoom";
    }

    // 스터디룸 페이지
    @GetMapping("/studyroom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
    public String studyRoom(@RequestParam long metaIdx, Principal principal, Model model) { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 넘겨준다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 7. 받아온 방 번호를 서비스에 넘겨준다.
        Meta.rpEntrance rpEntrance = metaService.entrance(metaIdx, rpNickImage);
        // 14. 반환받은 DTO가 존재하는지 체크한다.
        // 14-1. 반환받은 DTO가 없는 경우
        if ( rpEntrance == null ) {
            // 14-1-1. 에러메세지를 바인딩한다.
            model.addAttribute("err", "해당 방의 정보가 없습니다.");
            // 메타 메인 페이지로 이동
            return "Meta/MetaRoom";
        // 14-2. 반환받은 DTO가 있는 경우
        } else {
            // 15. 반환받은 DTO 값중 metaIdx가 0인지 아닌지 체크한다.
            // 15-1. metaIdx가 0인 경우 - 모집인원이 정원초과
            if ( rpEntrance.getMetaIdx() == 0 ) {
                // 15-1-1. metaTitle에 저장한 에러메세지를 바인딩한다.
                model.addAttribute("err", rpEntrance.getMetaTitle());
                // 메타 메인 페이지로 이동
                return "Meta/MetaRoom";
            // 15-2. metaIdx가 0이 아닌 경우 - 해당 방에 입장
            } else {
                // 15-2-1. 방 번호와 2에서 반환받은 DTO와 7에서 반환받은 DTO를 서비스에 넘겨준다.
                List<MetaRoom.rpMetaRoomIdxList> rpMetaRoomIdxList = metaService.metaRoomParticipant(metaIdx, rpNickImage, rpEntrance);
                // 20. 15-2-1에서 반환받은 DTO를 바인딩한다.
                model.addAttribute("participantList", rpMetaRoomIdxList);
                // 21. 7에서 반환받은 DTO를 바인딩한다.
                model.addAttribute("metaRoom", rpEntrance);
                // 22. 2에서 반환받은 DTO를 바인딩한다.
                model.addAttribute("nickImage", rpNickImage);
                // 스터디룸 페이지로 이동
                return "Meta/StudyRoom";
            }
        }
    }

//    // 카페 페이지
//    @GetMapping("/caferoom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
//    public String cafeRoom(@RequestParam long metaIdx, Principal principal, Model model) { // 1. 파라미터로 입장한 방 번호를 받아온다.
//        // 2. 받아온 방 번호를 서비스에 넘겨준다.
//        MetaRoom.rpEntrance rpEntrance = metaService.entrance(metaIdx);
//        // 7. 반환받은 DTO가 존재하는지 체크한다.
//        // 7-1. 반환받은 DTO가 없는 경우
//        if ( rpEntrance == null ) {
//            // 에러메세지를 바인딩한다.
//            model.addAttribute("err", "해당 방의 정보가 없습니다.");
//            // 메타 메인 페이지로 이동
//            return "Meta/MetaRoom";
//            // 7-2. 반환받은 DTO가 있는 경우
//        } else {
//            // 8. 반환받은 DTO 값중 metaIdx가 0인지 아닌지 체크한다.
//            // 8-1. metaIdx가 0인 경우 - 모집인원이 정원초과
//            if ( rpEntrance.getMetaIdx() == 0 ) {
//                // 8-1-1. metaTitle에 지정한 에러메세지를 바인딩한다.
//                model.addAttribute("err", rpEntrance.getMetaTitle());
//                // 메타 메인 페이지로 이동
//                return "Meta/MetaRoom";
//                // 8-2. metaIdx가 0이 아닌 경우 - 해당 방에 입장
//            } else {
//                // 8-2-1. 반환받은 DTO를 바인딩한다,
//                model.addAttribute("meta", rpEntrance);
//                // 8-2-2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 넘겨준다.
//                Member.rpNickName rpNickName = signUpService.memberNickname(principal.getName());
//                // 13. 반환받은 DTO를 바인딩한다.
//                model.addAttribute("nickname", rpNickName);
//                // 카페 페이지로 이동
//                return "Meta/CafeRoom";
//            }
//        }
//    }
//
//    // 자습실 페이지
//    @GetMapping("/oneroom") // Principal - 자바의 표준 시큐리티 기술로, 로그인 유저의 정보를 담고 있다.
//    public String oneRoom(@RequestParam long metaIdx, Principal principal, Model model) { // 1. 파라미터로 입장한 방 번호를 받아온다.
//        // 2. 받아온 방 번호를 서비스에 넘겨준다.
//        MetaRoom.rpEntrance rpEntrance = metaService.entrance(metaIdx);
//        // 7. 반환받은 DTO가 존재하는지 체크한다.
//        // 7-1. 반환받은 DTO가 없는 경우
//        if ( rpEntrance == null ) {
//            // 에러메세지를 바인딩한다.
//            model.addAttribute("err", "해당 방의 정보가 없습니다.");
//            // 메타 메인 페이지로 이동
//            return "Meta/MetaRoom";
//            // 7-2. 반환받은 DTO가 있는 경우
//        } else {
//            // 8. 반환받은 DTO 값중 metaIdx가 0인지 아닌지 체크한다.
//            // 8-1. metaIdx가 0인 경우 - 모집인원이 정원초과
//            if ( rpEntrance.getMetaIdx() == 0 ) {
//                // 8-1-1. metaTitle에 지정한 에러메세지를 바인딩한다.
//                model.addAttribute("err", rpEntrance.getMetaTitle());
//                // 메타 메인 페이지로 이동
//                return "Meta/MetaRoom";
//                // 8-2. metaIdx가 0이 아닌 경우 - 해당 방에 입장
//            } else {
//                // 8-2-1. 반환받은 DTO를 바인딩한다,
//                model.addAttribute("meta", rpEntrance);
//                // 8-2-2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 넘겨준다.
//                Member.rpNickName rpNickName = signUpService.memberNickname(principal.getName());
//                // 13. 반환받은 DTO를 바인딩한다.
//                model.addAttribute("nickname", rpNickName);
//                // 자습실 페이지로 이동
//                return "Meta/OneRoom";
//            }
//        }
//    }

    // 방 나가기
    @GetMapping("/exit")
    public String exitRoom(@RequestParam long metaIdx, Principal principal) { // 1. 파라미터로 입장한 방 번호를 받아온다.
        // 2. Principal을 사용하여 로그인 유저의 아이디를 서비스에 넘겨준다.
        Member.rpNickImage rpNickImage = signUpService.memberNickImage(principal.getName());
        // 3. 받아온 방 번호를 서비스에 넘겨준다.
        metaService.exit(metaIdx, rpNickImage);
        // 메타 메인 페이지로 이동
        return "redirect:/meta";
    }
}
