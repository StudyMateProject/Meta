package com.study.mate.service;

import com.study.mate.entity.*;
import com.study.mate.repository.*;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@Builder
public class PayService {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    PayRepository payRepository;

    @Autowired
    StoreLikeRepository storeLikeRepository;

    @Autowired
    StoreCommentRepository storeCommentRepository;

    //이메일로 멤버 검색 메서드
    public Member findAll(String emailId){
        Member member = memberRepository.findByEmailId(emailId);
        return member;
    }

    //이메일로 닉네임을 검색하는 메서드
    public String returnNickname(String emailId) {
        //이메일로 멤버 객체 검색
        Member member = memberRepository.findByEmailId(emailId);
        //닉네임에 검색한 멤버 객체 닉네임 저장
        String nickname = member.getNickname();
        //닉네임 리턴
        return nickname;
    }

    //카테고리로 스토어 리스트를 검색하는 메서드
    public List<Store> findList(String category){
        //카테고리로 스토어 리스트 검색
        List<Store> list = storeRepository.findByCategory(category);
        //검색한 스토어 리스트 리턴
        return list;
    }

    //상품 이름으로 상품정보를 검색하는 메서드
    public Store findStore(String itemName){
        //상품 이름으로 스토어 객체 검색
        Store payItem = storeRepository.findByItemName(itemName);
        //검색된 스토어 객체 리턴
        return payItem;
    }

    //결제 완료된 항목을 데이터베이스에 저장하는 메서드
    public void insertPay(Pay requestPay){
        //결제가 완료 되었으면 0로 저장
        requestPay.setIsPaid(0);
        //결제 완료된 항목 데이터베이스에 저장
        payRepository.save(requestPay);
    }

    //이메일로 구매내역 리스트를 검색하는 메서드
    public List<Pay> findPay(String emailId){
        //이메일로 구매내역 리스트를 검색
        List<Pay> payList = payRepository.findByBuyerEmail(emailId);
        //구매내역 리스트 리턴
        return payList;
    }

    //impUid 로 구매내역을 검색하는 메서드
    public Pay refundCheck(String impUid){
        //impUid 로 구매내역 객체를 검색
        Pay resPay = payRepository.findByImpUid(impUid);
        //구매내역 객체 리턴
        return resPay;
    }

    //환불된 객체를 저장하는 메서드
    public void updatePay(Pay pay){
        //결제 완료 된것을 환불로 변경하기 위해서 1로 고정
        pay.setIsPaid(1);
        //환불중 으로 고친 값을 데이터베이스에 저장
        payRepository.save(pay);
    }

    //페이징 처리를 위해 상품항목으로 모든 열 갯수 검색하는 메서드
    public int rowTotal(String goods) {
        //상품 항목으로 모든 열 갯수를 검색한 이후 갯수를 리턴
        return Long.valueOf(storeRepository.countByGoods(goods)).intValue();
    }

    //상품 항목과 페이징 처리를 위해서 들어온 map 을 사용해서 리스트를 검색하는 메서드
    public List<Store> storeListAll(HashMap<String, Integer> map, String goods) {
        //상품 항목과 페이징 처리를 위해서 들어온 map 을 이용해서 리스트를 검색
        List<Store> storeList = storeRepository.findStoreList(map.get("start"), map.get("end"), goods);
        //검색한 리스트 리턴
        return storeList;
    }

    //이메일을 이용해서 멤버 idx 를 검색하는 메서드
    public long returnIdx(String emailId) {
        //이메일을 이용해서 멤버 객체 검색
        Member member = memberRepository.findByEmailId(emailId);
        //검색된 멤버를 이용해서 idx 리턴
        return member.getIdx();
    }

    //스토어 idx 와 멤버 idx 를 이용해서 좋아요를 했는지 검색하는 메서드
    public int likeCheck(long storeIdx, long memberIdx) {
        //결과값을 저장할 변수
        int check = 0;
        //스토어 idx 와 멤버 idx 를 이용해서 스토어 좋아요 객체 검색
        StoreLike storeLike = storeLikeRepository.findByLikeIdxAndMemberIdx(storeIdx, memberIdx);
        //스토어 좋아요 객체가 있다면 체크값 변경
        if(storeLike != null) {
            check = 1;
        }
        //결과값 리턴
        return check;
    }

    //스토어 좋아요 객체를 이용해서 좋아요 업데이트
    public Store likeUpdate(StoreLike storeLike) {
        //스토어 좋아요 객체를 검색
        StoreLike storeLike1 = storeLikeRepository.findByLikeIdxAndMemberIdx(storeLike.getLikeIdx(), storeLike.getMemberIdx());
        //스토어 객체 생성
        Store afterStore = null;
        //만약 스토어 좋아요 객체가 없다면
        if(storeLike1 == null) {
            //스토어 좋아요 객체 데이터베이스에 저장
            storeLikeRepository.save(storeLike);
            //스토어 idx 로 스토어 좋아요 갯수 업데이트
            storeRepository.updateStoreLikeCount(storeLike.getLikeIdx());
            //생성한 스토어 객체에 스토어 idx 로 검색한 이후 객체에 저장
            afterStore = storeRepository.findByStoreIdx(storeLike.getLikeIdx());
            //스토어 좋아요 객체가 있다면
        } else {
            //스토어 좋아요 객체 삭제
            storeLikeRepository.delete(storeLike1);
            //스토어 idx 로 스토어 좋아요 갯수 업데이트
            storeRepository.updateStoreLikeCount(storeLike1.getLikeIdx());
            //생성한 스토어 객체에 스토어 idx 로 검색한 이후 객체에 저장
            afterStore = storeRepository.findByStoreIdx(storeLike1.getLikeIdx().intValue());
        }
        //스토어 객체 리턴
        return afterStore;
    }

    //구매한 사람만 댓글을 작성할 수 있도록 확인하는 결제내역 리스트 검색 메서드
    public List<Pay> commentList(String itemName) {
        //상품명으로 결제내역 리스트 검색 후 리턴
        return payRepository.commentList(itemName);
    }

    //스토어 댓글 리스트 검색 메서드
    public List<StoreComment> storeCommentList() {
        //스토어 댓글 전체 리스트 검색 후 리턴
        return storeCommentRepository.findAll();
    }

    //스토어 댓글 저장 메서드
    public String saveComment(StoreComment storeComment) {
        //결과값 저장 메서드
        String res = "no";
        //스토어 댓글 메서드가 있다면
        if(storeComment != null) {
            //스토어 댓글 저장
            storeCommentRepository.save(storeComment);
            //결과값 변수 값 변경
            res = "yes";
        }
        //결과값 변수 리턴
        return res;
    }

    //스토어 댓글 삭제 메서드
    public String deleteComment(long idx) {
        //결과값 저장 메서드
        String res = "no";
        //댓글 idx 로 스토어 댓글 객체 검색
        StoreComment deleteComment = storeCommentRepository.findByIdx(idx);
        //스토어 댓글 객체가 있다면
        if(deleteComment != null) {
            //스토어 댓글 삭제 처리
            deleteComment.setDeleteCheck(1);
            //스토어 댓글 업데이트
            storeCommentRepository.save(deleteComment);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }

    //댓글 수정
    public String modifyComment(StoreComment storeComment) {
        //결과값 저장 메서드
        String res = "no";
        //댓글 idx 로 스토어 댓글 객체 검색
        StoreComment beforeModify = storeCommentRepository.findByIdx(storeComment.getIdx());
        //스토어 댓글 객체가 있다면
        if(beforeModify != null) {
            //스토어 댓글 내용 변경 처리
            beforeModify.setComment(storeComment.getComment());
            //스토어 댓글 업데이트
            storeCommentRepository.save(beforeModify);
            //결과값 변경
            res = "yes";
        }
        //결과값 리턴
        return res;
    }
}
