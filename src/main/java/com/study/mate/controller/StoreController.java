package com.study.mate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.study.mate.entity.*;
import com.study.mate.httpclient.IamPortPass;
import com.study.mate.service.PayService;
import com.study.mate.util.PageSetup;
import com.study.mate.util.Paging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

@Controller
//맨 앞에 '/store' 라는 url 값이 들어와야지만 스토어에 관련된 페이지로 이동
@RequestMapping("/store")
public class StoreController {

    @Autowired
    PayService payService;

    //스토어 메인페이지
    @GetMapping("")
    public String store(){
        return"Store/Store";
    }

    //멤버쉽리스트 페이지
    @GetMapping("/membershiplist")
    public String membershipList(Model model, @RequestParam(value="page", defaultValue="0") int page){
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String goods = "membership";
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
        int rowTotal = payService.rowTotal(goods);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<Store> storeList = payService.storeListAll(map, goods);
        model.addAttribute("categoryList", storeList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("membershiplist", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Store/MemberShipList";
    }

    //책 리스트 페이지
    @GetMapping("/booklist")
    public String bookList(Model model, @RequestParam(value="page", defaultValue="0") int page){
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String goods = "book";
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
        int rowTotal = payService.rowTotal(goods);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<Store> storeList = payService.storeListAll(map, goods);
        model.addAttribute("categoryList", storeList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("booklist", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Store/BookList";
    }

    //기타 리스트 페이지
    @GetMapping("/etclist")
    public String etcList(Model model, @RequestParam(value="page", defaultValue="0") int page){
        //현재 페이지의 숫자를 가지고 그 값에 맞는 리스트를 끌어온다.
        //페이징 처리
        //불러올 리스트 타입 저장
        String goods = "etc";
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
        int rowTotal = payService.rowTotal(goods);
        //페이징 처리를 위해 맵을 이용해서 리스트 가져오기
        List<Store> storeList = payService.storeListAll(map, goods);
        model.addAttribute("categoryList", storeList);
        //페이징 처리를 위해 url, 이동할 페이지번호, 전체 열의 갯수, 페이징을 위해 설정을 잡아둔 값들을 가지고 HTML 에 작성해줄 내용을 생성한다.
        String pageMenu = Paging.getPaging("etclist", nowPage, rowTotal, PageSetup.BLOCKLIST, PageSetup.BLOCKPAGE);
        model.addAttribute("pageMenu", pageMenu);
        return "Store/EtcList";
    }

    //상품 상세보기 페이지
    @GetMapping("/pay")
    public String pay(Store store, Model model, Principal principal){
        //사용자가 구매할 상품 검색
        Store buyItem = payService.findStore(store.getItemName());
        //모델 바인딩
        model.addAttribute("item", buyItem);
        //멤버 idx 검색
        long memberIdx = payService.returnIdx(principal.getName());
        //모델 바인딩
        model.addAttribute("memberIdx", memberIdx);
        //사용자가 상품에 좋아욜르 눌렀는지 검색
        int check = payService.likeCheck(buyItem.getStoreIdx(), memberIdx);
        //모델 바인딩
        model.addAttribute("likeCheck", check);
        //itemName, buyerEmail로 검색
        List<Pay> payList = payService.commentList(store.getItemName());
        //모델 바인딩
        model.addAttribute("payList", payList);
        //모델 바인딩
        model.addAttribute("emailId", principal.getName());
        //닉네임 검색
        String nickname = payService.returnNickname(principal.getName());
        //모델 바인딩
        model.addAttribute("nickname", nickname);
        //상품 댓글 리스트 검색
        List<StoreComment> storeCommentList = payService.storeCommentList();
        //모델 바인딩
        model.addAttribute("commentList", storeCommentList);
        return "Store/Pay";
    }

    //구매팝업 페이지
    @GetMapping("/purchase")
    //이전 페이지의 url 에서 Store 객체에 맞는 값들을 자동으로 파라미터로 받아온다.
    //이전 페이지의 url 에서 Principal 객체에 emailId 값을 담아서 파라미터로 받아온다.(스프링 시큐리티 부분)
    public String popupPay(Store store, Pay pay, Model model, Principal principal){
        //결제를 하려는 갯수를 저장한다.
        int itemCount = pay.getItemCount();
        //다음 팝업 페이지에서 사용할 수 있도록 모델에 값을 추가한다.
        model.addAttribute("itemCount", itemCount);
        //결제를 하려는 항목을 itemName 값으로 불러온다.
        Store payItem = payService.findStore(store.getItemName());
        //다음 팝업 페이지에서 사용할 수 있도록 모델에 Store 객체를 저장한다.
        model.addAttribute("item", payItem);
        //principal 에 저장된 emailId 값을 따로 변수에 저장한다.
        String emailId = principal.getName();
        //저장한 emailId 값으로 멤버 정보를 가지고 온다.
        Member member = payService.findAll(emailId);
        //다음 팝업 페이지에서 사용할 수 있도록 모델에 Member 객체를 저장한다.
        model.addAttribute("member", member);
        return "Store/PayPopup";
    }

    //PayPopup 페이지에서 Request 를 보낸걸 컨트롤러에서 받아올 수 있도록 어노테이션을 붙여준다
    //url 주소는 '/complete', 메서드는 'POST' 방식으로 들어온 값만 받을 수 있게 설정한다.
    @RequestMapping(value = {"/complete"}, method = {RequestMethod.POST})
    //ResponseBody 라는 어노테이션으로 방금 받아온 값들을 처리해서 결과값을 다시 이전 페이지로 보내준다.
    @ResponseBody
    public String payComplete(Pay pay){
        JsonNode jsonToken = IamPortPass.getToken(); //서버로 부터 토큰값 받아옴 (Json 형식)
        String accessToken = jsonToken.get("response").get("access_token").asText(); //서버로 부터 토큰값 받아옴 (Text)

        JsonNode payment = IamPortPass.getUserInfo(pay.getImpUid(), accessToken);
        System.out.println(payment);
        //가져온 json 형식의  payment 를 문자열로 변환
        String status = payment.get("response").get("status").asText();
        String resImpUid = payment.get("response").get("imp_uid").asText();
        String resMerchantUid = payment.get("response").get("merchant_uid").asText();

        String res = "no";
        if(pay.getImpUid().equals(resImpUid)){
            if(pay.getMerchantUid().equals(resMerchantUid)){
                if(status.equalsIgnoreCase("paid")){
                    //만약 모든 결과값이 일치한다면 결제 완료된 정보를 데이터베이스에 저장
                    payService.insertPay(pay);
                    res = "yes";
                }

            }
        }
        return res;
    }

    //상품 상세보기 좋아요기능
    @GetMapping("/pay/like")
    @ResponseBody
    public String like(StoreLike storeLike) {
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //스토어 좋아요 업데이트
        Store afterStore = payService.likeUpdate(storeLike);
        //상품 글이 있다면
        if(afterStore != null) {
            //좋아요 갯수를 결과값 변수에 저장
            res = String.valueOf(afterStore.getGoodsLike());
        }
        //결과값 리턴
        return res;
    }

    //상품 상세보기 댓글기능
    @GetMapping("/pay/comment")
    @ResponseBody
    public String comment(StoreComment storeComment) {
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //상품 글에 댓글을 저장한 이후 결과값을 변수에 저장글
        res = payService.saveComment(storeComment);
        //결과값 리턴
        return res;
    }

    //댓글 수정
    @GetMapping("/pay/comment/modify")
    @ResponseBody
    public String commentModify(StoreComment storeComment) {
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //상품글에 댓글을 변경한 이후 결과값을 변수에 저장
        res = payService.modifyComment(storeComment);
        //결과값 리턴
        return res;
    }

    //댓글 삭제
    @GetMapping("/pay/comment/delete")
    @ResponseBody
    public String commentDelete(StoreComment storeComment){
        //결과값을 보내주는 결과값 변수
        String res = "no";
        //상품글에 댓글을 삭제한 이후 결과값을 변수에 저장
        res = payService.deleteComment(storeComment.getIdx());
        //결과값 리턴
        return res;
    }

}
