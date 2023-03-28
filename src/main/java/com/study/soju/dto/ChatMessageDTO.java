package com.study.soju.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessageDTO {
    private String type; // 메시지 타입
    private Long metaIdx; // 방 번호
    private String metaTitle; // 방 제목
    private String writer; // 작성자
    private String profileImage; // 참가자 프로필 사진
    private String message; // 메세지
    private int metaRecruitingPersonnel; // 참여중인 인원
    private String participant; // 새로운 참가자
    private String master; // 방장 닉네임
    private String masterProfileImage; // 방장 프로필 사진
    private String exit; // 퇴장 체크 값
    private String err; // 에러 체크 값
}
