package com.study.mate.controller;

import com.study.mate.entity.Alarm;
import com.study.mate.entity.RecruitStudy;
import com.study.mate.entity.RecruitStudyComment;
import com.study.mate.entity.RecruitStudyLike;
import com.study.mate.service.RecruitStudyService;
import com.study.mate.util.PageSetup;
import com.study.mate.util.Paging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/recruitstudy")
public class RecruitStudyController {

    @Autowired
    RecruitStudyService recruitStudyService;


    //스터디원 모집 메인페이지
    @GetMapping("")
    //value 에 페이지 라는 파라미터를 url 에서 가져온다. 만약 그 값이 없다면 0 으로 기본값을 잡아준다. 그값은 int 타입의 page 에 넣어준다.
    public String recruitStudy(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        // 한 페이지에 표시되는 게시물의 시작번호와 끝번호를 계산
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal();
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("recruitstudy", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("computer")
    public String recruitStudyComputer(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "computer";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        // 한 페이지에 표시되는 게시물의 시작번호와 끝번호를 계산
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("computer", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("math")
    public String recruitStudyMath(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "math";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("math", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("management")
    public String recruitStudyManagement(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "management";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("management", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("design")
    public String recruitStudyDesign(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "design";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("design", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("education")
    public String recruitStudyEducation(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "education";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("education", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("literature")
    public String recruitStudyLiterature(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "education";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("literature", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("entrance")
    public String recruitStudyEntrance(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "entrance";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("entrance", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("engineer")
    public String recruitStudyEngineer(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "engineer";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("engineer", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("language")
    public String recruitStudyLanguage(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "language";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("language", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("employment")
    public String recruitStudyEmployment(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "employment";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("employment", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }

    @GetMapping("etc")
    public String recruitStudyEte(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String studyType = "etc";
        int nowPage = 1;
        //만약 페이지값이 있다면 nowPage 값을 파라미터로 넘어온 page 값으로 변경
        if(page != 0) {
            nowPage = page;
        }
        int start = (nowPage - 1) * PageSetup.BLOCKLIST + 1;
        int end = start + PageSetup.BLOCKLIST - 1;
        //목록의 처음과 끝을 저장할 맵 생성
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("start", start);
        map.put("end", end);
        //전체 열의 갯수를 가지고온다.
        int rowTotal = recruitStudyService.rowTotal(studyType);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitStudy> recruitStudyList = recruitStudyService.recruitStudyListAll(map, studyType);
        model.addAttribute("list", recruitStudyList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("etc", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitStudy";
    }


    //스터디원 모집 작성페이지
    @GetMapping("/writeform")
    public String writeForm(Model model, Principal principal) {
        //사용자의 이메일 검색
        String emailId = principal.getName();
        //사용자의 닉네임 검색
        String nickname = recruitStudyService.returnNickname(emailId);
        //모델 바인딩
        model.addAttribute("nickname", nickname);
        return "Recruit/RecruitStudyWriteForm";
    }

    //스터디원 모집 글 작성
    @GetMapping("/writeform/write")
    public String write(RecruitStudy recruitStudy) {
        //서비스에 있는 메서드를 사용하기 위해서 멘티 객체를 파라미터로 전달 이후 저장
        recruitStudyService.writeRecruitStudy(recruitStudy);
        //다시 메인 창으로 돌아갈때 리스트를 들고 가야되기때문에 리다이렉트로 변경
        return "redirect:/recruitstudy";
    }

    //스터디원 모집 글 상세보기
    @GetMapping("/post")
    public String changeForm(Long idx, Model model, Principal principal) {
        //사용자의 이메일 검색
        String emailId = principal.getName();
        //사용자의 닉네임 검색
        String nickname = recruitStudyService.returnNickname(emailId);
        //모델 바인딩
        model.addAttribute("nickname", nickname);
        //스터디원 idx로 스터디원 구하기 글 검색
        RecruitStudy recruitStudy = recruitStudyService.findRecruitStudy(idx);
        //멤버 idx 검색
        long memberIdx = recruitStudyService.returnIdx(emailId);
        //모델 바인딩
        model.addAttribute("memberIdx", memberIdx);
        //사용자가 스터디원 구하기글에 좋아요를 눌렀는지 검색
        int count = recruitStudyService.likeCheck(idx, memberIdx);
        //스터디원 객체에 저장
        recruitStudy.setStudyLikeCheck(count);
        //모델 바인딩
        model.addAttribute("recruitStudy", recruitStudy);
        //알람 처리를 위해서 writer 의 emailId 를 넘겨준다.
        String email = recruitStudyService.returnEmailId(recruitStudy.getWriter());
        //모델 바인딩
        model.addAttribute("emailId", email);
        //스터디원구하기에 있는 댓글 리스트 검색
        List<RecruitStudyComment> recruitStudyCommentList = recruitStudyService.findCommentList(idx);
        //모델 바인딩
        model.addAttribute("list", recruitStudyCommentList);

        return "Recruit/RecruitStudyPost";
    }

    //스터디원 모집 글 상세보기 좋아요기능
    @GetMapping("/post/like")
    @ResponseBody
    public String like(RecruitStudyLike recruitStudyLike, Alarm alarm) {
        //알람을 만들어 주기 위해서 Type 추가
        alarm.setAlarmType(1);
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //스터디원구하기 좋아요 업데이트
        RecruitStudy afterRecruitStudy = recruitStudyService.likeUpdate(recruitStudyLike, alarm);
        //스터디원구하기 글이 있다면
        if(afterRecruitStudy != null) {
            //좋아요 갯수를 결과값 변수에 저장
            res = String.valueOf(afterRecruitStudy.getStudyLike());
        }
        //결과값 리턴
        return res;
    }

    //스터디원 모집글 상세보기 내용 변경 페이지
    @GetMapping("/post/modifyform")
    public String modifyForm(RecruitStudy recruitStudy, Model model) {
        //스터디운구하기 검색
        RecruitStudy recruitStudy1 = recruitStudyService.findRecruitStudy(recruitStudy.getIdx());
        //모델 바인딩
        model.addAttribute("recruitStudy", recruitStudy1);
        return "Recruit/RecruitStudyModifyForm";
    }

    //스터디원 모집글 상세보기 내용 변경 기능
    @GetMapping("/post/modifyform/modify")
    @ResponseBody
    public String modify(RecruitStudy recruitStudy){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //만약 스터디원구하기 글이 있다면
        if(recruitStudy != null) {
            //결과값 변수에 스터디원구하기 글을 업데이트하고 난 이후 결과값을 저장
            res = recruitStudyService.modify(recruitStudy);
        }
        //결과값 리턴
        return res;
    }

    //스터디원 모집글 삭제
    @GetMapping("/post/delete")
    @ResponseBody
    public String delete(RecruitStudy recruitStudy){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //삭제할 스터디원구하기 글 검색
        RecruitStudy deleteRecruitStudy = recruitStudyService.findRecruitStudy(recruitStudy.getIdx());
        //스터디원구하기 글 삭제이후 결과값 변수에 저장
        res = recruitStudyService.delete(deleteRecruitStudy);
        //결과값 리턴
        return res;
    }

    //댓글 작성
    @GetMapping("/post/comment")
    @ResponseBody
    public String comment(RecruitStudyComment recruitStudyComment, Alarm alarm){
        //알람을 만들어 주기 위해서 Type 추가
        alarm.setAlarmType(4);
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //스터디원구하기 글에 댓글을 저장한 이후 결과값을 변수에 저장글
        res = recruitStudyService.saveComment(recruitStudyComment, alarm);
        //결과값 리턴
        return res;
    }

    //댓글 삭제
    @GetMapping("/post/comment/delete")
    @ResponseBody
    public String commentDelete(RecruitStudyComment recruitStudyComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //스터디원모집글에 댓글을 삭제한 이후 결과값을 변수에 저장
        res = recruitStudyService.deleteComment(recruitStudyComment.getIdx());
        //결과값 리턴
        return res;
    }

    //댓글 수정
    @GetMapping("/post/comment/modify")
    @ResponseBody
    public String commentModify(RecruitStudyComment recruitStudyComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //스터디원모집글에 댓글을 변경한 이후 결과값을 변수에 저장
        res = recruitStudyService.modifyComment(recruitStudyComment);
        //결과값 리턴
        return res;
    }

    //스터디원 구하기 신청
    @GetMapping("/post/apply")
    @ResponseBody
    public String studyApply(RecruitStudy recruitStudy, Alarm alarm) {
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //알람을 생성하기 위해서 타입 넣어주기
        alarm.setAlarmType(2);
        //알림을 생성한 이후 결과값을 변수에 저장
        res = recruitStudyService.studyApply(alarm);
        //결과값 확인
        return res;
    }
}
