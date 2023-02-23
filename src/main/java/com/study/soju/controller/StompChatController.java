package com.study.soju.controller;

import com.study.soju.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.*;

@Controller
@RequiredArgsConstructor
public class StompChatController {
    @Autowired
    SimpMessagingTemplate template; // 특정 Broker로 메세지를 전달한다.

    // 메시지를 보낼 때 퇴장 메시지와 재입장 메시지를 관리하기 위한 ConcurrentHashMap
    ConcurrentHashMap<String, ChatMessageDTO> metaMessageMap = new ConcurrentHashMap<>();
    // ScheduledExecutorService를 생성합니다.
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Client에서 전송한 SEND 요청을 처리
    // @MessageMapping - 클라이언트에서 요청을 보낸 URI 에 대응하는 메소드로 연결을 해주는 역할을 한다.
    // StompWebSocketConfig에서 설정한 applicationDestinationPrefixes와 @MessageMapping 경로가 자동으로 병합된다.
    // "/pub" + "/meta/studyRoom/enter" = "/pub/meta/studyRoom/enter"
////////////////////////////////////////////////// 스터디룸 구역 //////////////////////////////////////////////////
    // 스터디룸 입장
    @MessageMapping(value = "/meta/studyRoom/enter")
    public void enterStudyRoom(ChatMessageDTO message) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. 1에서 받아온 DTO 값 중 작성자를 가져와 참여메세지를 작성해 DTO 값 중 메세지에 저장한다.
        message.setMessage(message.getWriter() + "님이 채팅방에 참여하였습니다.");
        // 3. 1에서 받아온 DTO 값 중 작성자를 가져와 참여자로 저장한다.
        message.setParticipant(message.getWriter());
        // 4. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 받아온 방 번호가 병합된다.
        //    "/sub" + "/meta/studyRoom" + metaIdx = "/sub/meta/studyRoom/1"
        template.convertAndSend("/sub/meta/studyRoom/" + message.getMetaIdx(), message);
    }

    @MessageMapping(value = "/meta/studyRoom/reenter")
    public void reEnterStudyRoom(ChatMessageDTO message, SimpMessageHeaderAccessor accessor) {
        String metaIdx = (String) accessor.getSessionAttributes().get("metaIdx");
        // 이전의 퇴장 메시지 삭제
        ChatMessageDTO exitMessage = metaMessageMap.get(metaIdx + "_exit");
        if (exitMessage != null) {
            metaMessageMap.remove(metaIdx + "_exit");
        }

        template.convertAndSend("/sub/meta/studyRoom/" + message.getMetaIdx(), message);
    }

    // 스터디룸 채팅
    @MessageMapping(value = "/meta/studyRoom/message")
    public void messageStudyRoom(ChatMessageDTO message) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 받아온 방 번호가 병합된다.
        //    "/sub" + "/meta/studyRoom" + metaIdx = "/sub/meta/studyRoom/1"
        template.convertAndSend("/sub/meta/studyRoom/" + message.getMetaIdx(), message);
    }

    // 스터디룸 퇴장
    @MessageMapping(value = "/meta/studyRoom/exit")
    public CompletableFuture<Void> exitStudyRoom(ChatMessageDTO message, SimpMessageHeaderAccessor accessor) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. 받아온 DTO 값 중 작성자를 가져와 퇴장메세지를 작성해 DTO 값 중 메세지에 저장한다.
        message.setMessage(message.getWriter() + "님이 채팅방에서 탈주하였습니다.");
        // 3. 1에서 받아온 DTO 값 중 작성자를 가져와 퇴장자로 저장한다.
        message.setExit(message.getWriter());
        // 4. 1에서 받아온 DTO 값 중 참가중인 인원을 가져와 1을 감소한뒤 다시 참가중인 인원에 저장한다.
        message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);

        String metaIdx = (String) accessor.getSessionAttributes().get("metaIdx");

        // 새로운 퇴장 메시지 추가
        metaMessageMap.put(metaIdx + "_exit", message);

        // 퇴장 메시지를 전송하기 전에 1초를 대기한다. - 새로고침 대기
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                ChatMessageDTO exitMessage = metaMessageMap.get(metaIdx + "_exit");
                if (exitMessage == null) {
                    System.out.println(1);
                    return;
                } else {
                    template.convertAndSend("/sub/meta/studyRoom/" + message.getMetaIdx(), message);
                    System.out.println(2);
                    metaMessageMap.remove(metaIdx + "_exit");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
////////////////////////////////////////////////// 카페 구역 //////////////////////////////////////////////////
    // 카페 입장
    @MessageMapping(value = "/meta/cafeRoom/enter")
    public void enterCafeRoom(ChatMessageDTO message) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. 받아온 DTO 값 중 작성자를 가져와 참여메세지를 작성해 DTO 값 중 메세지에 저장한다.
        message.setMessage(message.getWriter() + "님이 채팅방에 참여하였습니다.");
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1번에서 받아온 방 번호가 병합된다.
        //    "/sub" + "/meta/studyRoom" + metaIdx = "/sub/meta/cafeRoom/1"
        template.convertAndSend("/sub/meta/cafeRoom/" + message.getMetaIdx(), message);
    }

    // 카페 채팅
    @MessageMapping(value = "/meta/cafeRoom/message")
    public void messageCafeRoom(ChatMessageDTO message) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1번에서 받아온 방 번호가 병합된다.
        //    "/sub" + "/meta/studyRoom" + metaIdx = "/sub/meta/cafeRoom/1"
        template.convertAndSend("/sub/meta/cafeRoom/" + message.getMetaIdx(), message);
    }

    // 카페 퇴장
    @MessageMapping(value = "/meta/cafeRoom/exit")
    public void exitCafeRoom(ChatMessageDTO message) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. 받아온 DTO 값 중 작성자를 가져와 퇴장메세지를 작성해 DTO 값 중 메세지에 저장한다.
        message.setMessage(message.getWriter() + "님이 채팅방에서 탈주하였습니다.");
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1번에서 받아온 방 번호가 병합된다.
        //    "/sub" + "/meta/studyRoom" + metaIdx = "/sub/meta/cafeRoom/1"
        template.convertAndSend("/sub/meta/cafeRoom/" + message.getMetaIdx(), message);
    }
////////////////////////////////////////////////// 자습실 구역 //////////////////////////////////////////////////
    // 자습실 입장
    @MessageMapping(value = "/meta/oneRoom/enter")
    public void enterOneRoom(ChatMessageDTO message) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. 받아온 DTO 값 중 작성자를 가져와 참여메세지를 작성해 DTO 값 중 메세지에 저장한다.
        message.setMessage(message.getWriter() + "님이 채팅방에 참여하였습니다.");
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1번에서 받아온 방 번호가 병합된다.
        //    "/sub" + "/meta/studyRoom" + metaIdx = "/sub/meta/oneRoom/1"
        template.convertAndSend("/sub/meta/oneRoom/" + message.getMetaIdx(), message);
    }

    // 자습실 퇴장
    @MessageMapping(value = "/meta/oneRoom/exit")
    public void exitOneRoom(ChatMessageDTO message) { // 1. DTO로 채팅 정보들을 다 받아온다.
        // 2. 받아온 DTO 값 중 작성자를 가져와 퇴장메세지를 작성해 DTO 값 중 메세지에 저장한다.
        message.setMessage(message.getWriter() + "님이 채팅방에서 탈주하였습니다.");
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1번에서 받아온 방 번호가 병합된다.
        //    "/sub" + "/meta/studyRoom" + metaIdx = "/sub/meta/oneRoom/1"
        template.convertAndSend("/sub/meta/oneRoom/" + message.getMetaIdx(), message);
    }
}
