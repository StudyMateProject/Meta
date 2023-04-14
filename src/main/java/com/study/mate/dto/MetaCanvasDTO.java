package com.study.mate.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MetaCanvasDTO {
    private String type; // 메시지 타입
    private Long metaIdx; // 방 번호
    private String writer; // 참가자
    private String character; // 캐릭터
    private int x; // x축 좌표
    private int y; // y축 좌표
    private String characters; // 캐릭터 정보
    private int canvasLeft; // 캔버스 왼쪽 벽 좌표
    private int canvasTop; // 캔버스 위쪽 벽 좌표
    private int canvasRight; // 캔버스 오른쪽 벽 좌표
    private int canvasBottom; // 캔버스 아래쪽 벽 좌표
}
