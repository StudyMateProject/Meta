package com.study.soju.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MetaCanvasDTO {
    private Long metaIdx; // 방 번호
    private String writer; // 참가자
    private String character; // 캐릭터
    private String type;
    private int x;
    private int y;
    private String characters;
    private int canvasLeft;
    private int canvasTop;
    private int canvasRight;
    private int canvasBottom;
}
