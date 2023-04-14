package com.study.mate.controller;

import com.study.mate.entity.Alarm;
import com.study.mate.entity.RecruitMentee;
import com.study.mate.entity.RecruitMenteeComment;
import com.study.mate.entity.RecruitMenteeLike;
import com.study.mate.service.RecruitMenteeService;
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
@RequestMapping("/mentorprofilelist")
public class RecruitMenteeController {

    @Autowired
    RecruitMenteeService recruitMenteeService;

    //멘티 구하기 메인 페이지
    @GetMapping("")
    public String recruitMenteeList(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
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
        int rowTotal = recruitMenteeService.rowTotal();
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitMentee> recruitMenteeList = recruitMenteeService.recruitMenteeListAll(map);
        model.addAttribute("list", recruitMenteeList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("mentorprofilelist", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitMenteeList";
    }

    //멘티 구하기 글 작성 페이지
    @GetMapping("/writeform")
    public String writeForm(Model model, Principal principal) {
        //사용자의 이메일 검색
        String emailId = principal.getName();
        //사용자의 닉네임 검색
        String nickname = recruitMenteeService.returnNickname(emailId);
        //모델 바인딩
        model.addAttribute("nickname", nickname);
        return "Recruit/RecruitMenteeWriteForm";
    }

    //멘티 구하기 글 작성
    @GetMapping("/writeform/write")
    public String write(RecruitMentee recruitMentee) {
        //서비스에 있는 메서드를 사용하기 위해서 멘티 객체를 파라미터로 전달 이후 저장
        recruitMenteeService.writeRecruitMentee(recruitMentee);
        //다시 메인 창으로 돌아갈때 리스트를 들고 가야되기때문에 리다이렉트로 변경
        return "redirect:/mentorprofilelist";
    }

    //멘티 글 상세보기
    @GetMapping("/post")
    public String post(Long idx, Model model, Principal principal){
        //사용자의 이메일 검색
        String emailId = principal.getName();
        //사용자의 닉네임 검색
        String nickname = recruitMenteeService.returnNickname(emailId);
        //모델 바인딩
        model.addAttribute("nickname", nickname);
        //멘티 idx로 멘티모집 글 검색
        RecruitMentee recruitMentee = recruitMenteeService.findRecruitMentee(idx);
        //멤버 idx 검색
        long memberIdx = recruitMenteeService.returnIdx(emailId);
        //모델 바인딩
        model.addAttribute("memberIdx", memberIdx);
        //사용자가 멘티모집 글에 좋아요를 눌렀는지 검색
        int count = recruitMenteeService.likeCheck(idx, memberIdx);
        //멘티 객체에 저장
        recruitMentee.setStudyLikeCheck(count);
        //모델 바인딩
        model.addAttribute("recruitMentee", recruitMentee);
        //알람 처리를 위해서 writer 의 emailId 를 넘겨준다.
        String email = recruitMenteeService.returnEmailId(recruitMentee.getWriter());
        //모델 바인딩
        model.addAttribute("emailId", email);
        //멘티모집에 있는 댓글 리스트 검색
        List<RecruitMenteeComment> recruitMenteeCommentList = recruitMenteeService.findCommentList(idx);
        //모델 바인딩
        model.addAttribute("list", recruitMenteeCommentList);
        return "Recruit/RecruitMenteePost";
    }

    //멘티 글 좋아요
    @GetMapping("/post/like")
    @ResponseBody
    public String like(RecruitMenteeLike recruitMenteeLike, Alarm alarm) {
        //알람을 만들어 주기 위해서 Type 추가
        alarm.setAlarmType(1);
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘티모집 좋아요 업데이트
        RecruitMentee afterRecruitMentee = recruitMenteeService.likeUpdate(recruitMenteeLike, alarm);
        //만약 멘티모집 글이 있다면
        if(afterRecruitMentee != null) {
            //좋아요 갯수를 결과값 변수에 저장
            res = String.valueOf(afterRecruitMentee.getStudyLike());
        }
        //결과값 리턴
        return res;
    }

    //멘티 글 수정 페이지
    @GetMapping("/post/modifyform")
    public String modifyForm(RecruitMentee recruitMentee, Model model) {
        //멘티모집 검색
        RecruitMentee recruitMentee1 = recruitMenteeService.findRecruitMentee(recruitMentee.getIdx());
        //모델 바인딩
        model.addAttribute("recruitMentee", recruitMentee1);
        return "Recruit/RecruitMenteeModifyForm";
    }

    //멘티 글 수정
    @GetMapping("/post/modifyform/modify")
    @ResponseBody
    public String modify(RecruitMentee recruitMentee) {
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //만약 멘티모집글이 있다면
        if(recruitMentee != null) {
            //결과값 변수에 멘티모집글을 업데이트하고 난 이후 결과값을 저장
            res = recruitMenteeService.modify(recruitMentee);
        }
        //결과값 리턴
        return res;
    }

    //멘티 글 삭제
    @GetMapping("/post/delete")
    @ResponseBody
    public String delete(RecruitMentee recruitMentee){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //삭제할 멘티모집글 검색
        RecruitMentee deleteRecruitMentee = recruitMenteeService.findRecruitMentee(recruitMentee.getIdx());
        //멘티모집글 삭제이후 결과값 변수에 저장
        res = recruitMenteeService.delete(deleteRecruitMentee);
        //결과값 리턴
        return res;
    }

    //댓글 작성
    @GetMapping("/post/comment")
    @ResponseBody
    public String comment(RecruitMenteeComment recruitMenteeComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘티모집 글에 댓글을 저장한 이후 결과값을 변수에 저장
        res = recruitMenteeService.saveComment(recruitMenteeComment);
        //결과값 리턴
        return res;
    }

    //댓글 삭제
    @GetMapping("/post/comment/delete")
    @ResponseBody
    public String commentDelete(RecruitMenteeComment recruitMenteeComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘티모집 글에 댓글을 삭제한 이후 결과값을 변수에 저장
        res = recruitMenteeService.deleteComment(recruitMenteeComment.getIdx());
        //결과값 리턴
        return res;
    }

    //댓글 수정
    @GetMapping("/post/comment/modify")
    @ResponseBody
    public String commentModify(RecruitMenteeComment recruitMenteeComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘티모집 글에 댓글을 변경한 이후 결과값을 변수에 저장
        res = recruitMenteeService.modifyComment(recruitMenteeComment);
        //결과값 리턴
        return res;
    }

    //스터디원 구하기 신청
    @GetMapping("/post/apply")
    @ResponseBody
    public String menteeApply(RecruitMentee recruitMentee, Alarm alarm) {
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //알람을 생성하기 위해서 타입 넣어주기
        alarm.setAlarmType(2);
        //알람을 생성한 이후 결과값을 변수에 저장
        res = recruitMenteeService.menteeApply(alarm);
        //결과값 리턴
        return res;
    }
}
