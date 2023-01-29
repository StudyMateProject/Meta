package com.study.soju.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessageDTO {
    private String metaIdx; // 방 번호
    private String writer; // 작성자
    private String message; // 메세지
    private String participant; // 참가자
    private String profileImage; // 참가자 프로필 사진
    private String exit;
}
