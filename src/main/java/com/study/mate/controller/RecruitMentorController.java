package com.study.mate.controller;

import com.study.mate.entity.Alarm;
import com.study.mate.entity.RecruitMentor;
import com.study.mate.entity.RecruitMentorComment;
import com.study.mate.entity.RecruitMentorLike;
import com.study.mate.service.RecruitMentorService;
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
@RequestMapping("/recruitmentorlist")
public class RecruitMentorController {

    @Autowired
    RecruitMentorService recruitMentorService;

    //멘토모집 메인페이지
    @GetMapping("")
    public String recruitMentorList(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
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
        int rowTotal = recruitMentorService.rowTotal();
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<RecruitMentor> recruitMentorList = recruitMentorService.recruitMentorListAll(map);
        model.addAttribute("list", recruitMentorList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("recruitmentorlist", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Recruit/RecruitMentorList";
    }

    //멘토모집 글 작성 페이지
    @GetMapping("/writeform")
    public String writeForm(Model model, Principal principal) {
        //사용자의 이메일 검색
        String emailId = principal.getName();
        //사용자의 닉네임 검색
        String nickname = recruitMentorService.returnNickname(emailId);
        //모델 바인딩
        model.addAttribute("nickname", nickname);
        return "Recruit/RecruitMentorWriteForm";
    }

    //멘토모집 글 작성
    @GetMapping("/writeform/write")
    public String write(RecruitMentor recruitMentor) {
        //서비스에 있는 메서드를 사용하기 위해서 멘티 객체를 파라미터로 전달 이후 저장
        recruitMentorService.writeRecruitMentor(recruitMentor);
        //다시 메인 창으로 돌아갈때 리스트를 들고 가야되기때문에 리다이렉트로 변경
        return "redirect:/recruitmentorlist";
    }

    //멘토모집 글 상세보기
    @GetMapping("/post")
    public String postForm(Long idx, Model model, Principal principal){
        //사용자의 이메일 검색
        String emailId = principal.getName();
        //사용자의 닉네임 검색
        String nickname = recruitMentorService.returnNickname(emailId);
        //모델 바인딩
        model.addAttribute("nickname", nickname);
        //멘토 idx로 멘토모집 글 검색
        RecruitMentor recruitMentor = recruitMentorService.findRecruitMentor(idx);
        //멤버 idx 검색
        long memberIdx = recruitMentorService.returnIdx(emailId);
        //모델 바인딩
        model.addAttribute("memberIdx", memberIdx);
        //사용자가 멘토모집 글에 좋아요를 눌렀는지 검색
        int count = recruitMentorService.likeCheck(idx, memberIdx);
        //멘토 객체에 저장
        recruitMentor.setStudyLikeCheck(count);
        //모델 바인딩
        model.addAttribute("recruitMentor", recruitMentor);
        //알람 처리를 위해서 writer 의 emailId 를 넘겨준다.
        String email = recruitMentorService.returnEmailId(recruitMentor.getWriter());
        //모델 바인딩
        model.addAttribute("emailId", email);
        //멘토모집에 있는 댓글 리스트 검색
        List<RecruitMentorComment> recruitMentorCommentList = recruitMentorService.findCommentList(idx);
        //모델 바인딩
        model.addAttribute("list", recruitMentorCommentList);
        return "Recruit/RecruitMentorPost";
    }

    //멘토 글 좋아요
    @GetMapping("/post/like")
    @ResponseBody
    public String like(RecruitMentorLike recruitMentorLike, Alarm alarm) {
        //알람을 만들어 주기 위해서 Type 추가
        alarm.setAlarmType(1);
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘토모집 좋아요 업데이트
        RecruitMentor afterRecruitMentor = recruitMentorService.likeUpdate(recruitMentorLike, alarm);
        //만약 멘토모집 글이 있다면
        if(afterRecruitMentor != null) {
            //좋아요 갯수를 결과값 변수에 저장
            res = String.valueOf(afterRecruitMentor.getStudyLike());
        }
        //결과값 리턴
        return res;
    }

    //멘토 글 수정 페이지
    @GetMapping("/post/modifyform")
    public String postModify(Long idx, Model model) {
        //멘토모집 검색
        RecruitMentor recruitMentor = recruitMentorService.findRecruitMentor(idx);
        //모델 바인딩
        model.addAttribute("recruitMentor", recruitMentor);
        return "Recruit/RecruitMentorModifyForm";
    }

    //멘토 글 수정
    @GetMapping("/post/modifyform/modify")
    @ResponseBody
    public String modify(RecruitMentor recruitMentor){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //만약 멘토모집글이 있다면
        if(recruitMentor != null) {
            //결과값 변수에 멘토모집글을 업데이트하고 난 이후 걸과값을 저장
            res = recruitMentorService.modify(recruitMentor);
        }
        //결과값 리턴
        return res;
    }

    //멘토 글 삭제
    @GetMapping("/post/delete")
    @ResponseBody
    public String delete(RecruitMentor recruitMentor){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //삭제할 멘토모집글 검색
        RecruitMentor deleteRecruitMentor = recruitMentorService.findRecruitMentor(recruitMentor.getIdx());
        //멘토모집글 삭제이후 결과갑 변수에 저장
        res = recruitMentorService.delete(deleteRecruitMentor);
        //결과값 리턴
        return res;
    }

    //댓글작성
    @GetMapping("/post/comment")
    @ResponseBody
    public String comment(RecruitMentorComment recruitMentorComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘토모집 글에 댓글을 저장한 이후 결과값을 변수에 저장
        res = recruitMentorService.saveComment(recruitMentorComment);
        //결과값 리턴
        return res;
    }

    //댓글 삭제
    @GetMapping("/post/comment/delete")
    @ResponseBody
    public String commentDelete(RecruitMentorComment recruitMentorComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘토모집 글에 댓글을 삭제한 이후 결과값을 변수에 저장
        res = recruitMentorService.deleteComment(recruitMentorComment.getIdx());
        //결과값 리턴
        return res;
    }

    //댓글 수정
    @GetMapping("/post/comment/modify")
    @ResponseBody
    public String commentModify(RecruitMentorComment recruitMentorComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //멘토모집 글에 댓글을 변경한 이후 결과값을 변수에 저장
        res = recruitMentorService.modifyComment(recruitMentorComment);
        //결과값 리턴
        return res;
    }

    //멘토 구하기 신청
    @GetMapping("/post/apply")
    @ResponseBody
    public String mentorApply(RecruitMentor recruitMentor, Alarm alarm) {
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //알람을 생성하기 위해서 타입 넣어주기
        alarm.setAlarmType(2);
        //알람을 생성한 이후 결과값을 변수에 저장
        res = recruitMentorService.mentorApply(alarm);
        //결과값 리턴
        return res;
    }
}
