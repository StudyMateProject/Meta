package com.study.soju.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessageDTO {
    private Long metaIdx; // 방 번호
    private String metaTitle; // 방 제목
    private String type; // 메시지 타입
    private String writer; // 작성자
    private String message; // 메세지
    private String participant; // 참가자
    private String profileImage; // 참가자 프로필 사진
    private int metaRecruitingPersonnel; // 참여중인 인원
    private String exit; // 퇴장 체크값
    private String master; // 방장 닉네임
}
