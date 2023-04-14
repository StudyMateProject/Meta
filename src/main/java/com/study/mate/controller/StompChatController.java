package com.study.mate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.study.mate.dto.ChatMessageDTO;
import com.study.mate.dto.MetaCanvasDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class StompChatController {
    // @MessageMapping으로 받아온 메시지를 다시 클라이언트로 전달해주는 SimpMessagingTemplate
    @Autowired
    SimpMessagingTemplate template; // 특정 Broker로 메시지를 전달한다.

    @Autowired
    MetaController metaController;

    // JSON 형식의 데이터를 Java 객체로 역직렬화(Deserialize)하거나, 반대로 Java 객체를 JSON 형식의 데이터로 직렬화(Serialize)할 때 사용하는 Jackson 라이브러리의 클래스이다.
    @Autowired
    private ObjectMapper objectMapper;

    // RabbitMQ 메시징 서비스와 상호 작용하기 위한 스프링 AMQP 프로젝트의 클래스이다.
    // 이 클래스를 사용하여 RabbitMQ 큐로 메시지를 전송하고, 큐에서 메시지를 수신하여 이를 처리할 수 있다.
    // RabbitMQ의 메시지 브로커와 상호작용하면서 필요한 메시지 변환, 라우팅, 중복 처리 등의 기능을 제공한다.
    @Autowired
    RabbitTemplate rabbitTemplate;

    // RabbitMQ 서버에게 관리 작업을 수행하기 위한 API를 제공하는 클래스이다.
    @Autowired
    private RabbitAdmin rabbitAdmin;

    // 지정한 이름의 RabbitMQ 큐에 현재 쌓여있는 메시지 수를 반환한다.
    public int getMessageCount(String queueName) { // 파라미터로 큐 이름을 받아온다.
        // getQueueInfo - RabbitAdmin 객체의 메소드로서, 파라미터로 받아온 큐 이름의 큐 정보를 조회한다.
        // QueueInformation 객체로 반환되며, 해당 큐의 속성 정보와 현재 큐에 쌓여있는 메시지 수 등의 정보를 담고 있다.
        QueueInformation queueInformation = rabbitAdmin.getQueueInfo(queueName);
        // getMessageCount - QueueInformation 객체의 메소드로서, 현재 큐에 쌓여있는 메시지 수를 반환한다.
        return queueInformation.getMessageCount();
    }

    // 방장 재입장 체크를 위한 Map
    Map<Long, String> boomMetaRoom = new HashMap<>();

    // 방에 참가한 유저의 캐릭터 정보들을 보낼때 방 번호로 구분하기 위한 Map
    Map<Long, Map<String, List<Object>>> metaRoomMap = new HashMap<>();

    // 메시지를 보낼 때 퇴장 메시지와 재입장 메시지를 구분 관리하기 위한 ConcurrentHashMap
    ConcurrentHashMap<String, ChatMessageDTO> metaMessageMap = new ConcurrentHashMap<>();

    // Client에서 전송한 SEND 요청을 처리
    // @MessageMapping - 클라이언트에서 요청을 보낸 URI 에 대응하는 메소드로 연결을 해주는 역할을 한다.
    // StompWebSocketConfig에서 설정한 applicationDestinationPrefixes와 @MessageMapping 경로가 자동으로 병합된다.
    // "/pub" + "/meta/studyroom/enter" = "/pub/meta/studyroom/enter"
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////// 스터디룸 구역 //////////////////////////////////////////////////////
    // 스터디룸 첫 입장
    @MessageMapping(value = "/meta/studyroom/enter")
    public void enterStudyRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 첫 입장 정보들을 DTO로 받아온다.
        // 2. 먼저 위에서 생성한 방장 재입장 체크용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 값이 존재하는지 체크한다.
        // 2-1. Map에서 가져온 값이 존재하는 경우 - 방장 퇴장 후 1분 이내 유저 입장
        if ( boomMetaRoom.get(message.getMetaIdx()) != null ) {
            // 2-1-1. 그 다음 Map에서 가져온 값이 작성자와 같을 경우 - 방장 퇴장 후 1분 이내 방장 재입장
            if ( boomMetaRoom.get(message.getMetaIdx()).equals(message.getWriter()) ) {
                // 2-1-1-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "cancel"로 변경한다. - 방 삭제 취소 값
                message.setType("cancel");
                // 2-1-1-2. 퇴장 메소드에서 Map에 추가한 키에 해당하는 값을 삭제한다.
                boomMetaRoom.remove(message.getMetaIdx());
            }
        }
//        // 2-2. Map에서 가져온 값이 존재하지 않는 경우 - 방장 퇴장 전 입장
//        // 3. 1에서 파라미터로 받아온 DTO 값 중 에러 체크 값을 가져와 재입장 에러 값이 존재하는지 체크한다.
//        // 3-1. 재입장 에러 값이 존재하는 경우 - 재입장 메시지를 작성한다.
//        if ( message.getErr() != null && message.getErr().equals("reErr") ) {
//            // 3-1-1. 1에서 파라미터로 받아온 DTO 값 중 작성자(재입장자)와 방 제목을 가져와 재입장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
//            message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에 재입장하였습니다.");
//        // 3-2. 재입장 에러 값이 존재하지 않는 경우 - 첫 입장 메시지 작성한다.
//        } else {
//            // 3-2-1. 1에서 파라미터로 받아온 DTO 값 중 작성자(첫 입장자)와 방 제목을 가져와 입장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
//            message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에 입장하였습니다.");
//        }
        // 3. 1에서 파라미터로 받아온 DTO 값 중 작성자(첫 입장자)와 방 제목을 가져와 입장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
        message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에 입장하였습니다.");
        // 4. 1에서 파라미터로 받아온 DTO 값 중 작성자(첫 입장자)를 가져와 DTO 값 중 참가자에 setter를 통해 전달한다.
        message.setParticipant(message.getWriter());
        // 5. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
        template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
    }

    // 스터디룸 첫 입장 이후 재입장 - 첫 입장 이후 모든 재입장은 이곳으로 들어온다.
    @MessageMapping(value = "/meta/studyroom/reenter")
    public void reEnterStudyRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 재입장(새로고침) 정보들을 DTO로 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO 값 중 작성자(재입장자)를 가져와 DTO 값 중 참가자에 setter를 통해 전달한다.
        message.setParticipant(message.getWriter());
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
        template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
    }

//    // 스터디룸 첫 입장 이후 재입장 - 첫 입장 이후 모든 재입장은 이곳으로 들어온다. - 폐기
//    @MessageMapping(value = "/meta/studyroom/reenter")
//    public void reEnterStudyRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 재입장(새로고침) 정보들을 DTO로 받아온다.
//        // 2. 이전 퇴장 메소드에서 Map에 추가한 키에 해당하는 DTO를 다시 가져온다.
//        ChatMessageDTO exitMessage = metaMessageMap.get(message.getMetaIdx() + "_exit");
//        // 3. 2에서 가져온 DTO가 여전히 존재하는지 체크한다.
//        // 3-1. 퇴장 메시지가 존재하는 경우 - 재입장(새로고침)
//        if ( exitMessage != null ) {
//            // 3-1-1. 퇴장 메소드에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 삭제한다.
//            metaMessageMap.remove(message.getMetaIdx() + "_exit");
//            // 3-1-2. 1에서 파라미터로 받아온 DTO 값 중 작성자(재입장자)를 가져와 DTO 값 중 참가자에 setter를 통해 전달한다.
//            message.setParticipant(message.getWriter());
//            // 3-1-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//            //        "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
//            template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
//        // 3-2. 퇴장 메시지가 존재하지 않는 경우 - 0.3초가 넘는 장시간의 새로고침 에러로 인한 퇴장 처리 후 재입장된 것으로,
//        //                                 이는 아직 퇴장한 것이 아닌데 퇴장 처리가 되었으므로 다시 입장 처리를 해준다.
//        } else {
//            // 3-2-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "reErr"로 변경한다. - 재입장 에러 값
//            message.setType("reErr");
//            // 3-2-2. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다. - 재입장 에러로 퇴장
//            message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
//            // 3-2-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//            //        "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
//            template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
//        }
//    }

    // 스터디룸 채팅
    @MessageMapping(value = "/meta/studyroom/message")
    public void messageStudyRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 채팅 정보들을 DTO로 받아온다.
        // 2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 메시지 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
        template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
    }

    // 스터디룸 녹음
    @MessageMapping(value = "/meta/studyroom/record")
    public void recordStudyRoom(@Payload String recordFile,
                                SimpMessageHeaderAccessor accessor) { // 1. 클라이언트로부터 전송된 녹음된 오디오 데이터를 @Payload로 받아온다.
                                                                      //    @Payload - Spring 프레임워크에서 메시지 페이로드를 메소드 파라미터와 매핑할 때 사용되는 어노테이션이다.
                                                                      //               @Payload가 붙은 파라미터는 메시지 페이로드 자체를 나타내며, Spring은 메시지 변환기(MessageConverter)를 사용하여 해당 객체로 변환한다.
                                                                      //               일반적으로 Spring WebSocket이나 STOMP 메시지를 처리하는 메서드에서 많이 사용되며,
                                                                      //               이 어노테이션을 사용하면 개발자가 메시지 변환에 필요한 코드를 작성하지 않아도 되므로 코드 간결성과 생산성을 높일 수 있다.
                                                                      //    SimpMessageHeaderAccessor - Spring 프레임워크의 MessageHeaderAccessor 인터페이스를 구현한 클래스로, STOMP 메시지의 헤더 정보를 쉽게 조작하고 접근할 수 있게 해준다.
                                                                      //                                STOMP 프로토콜에서 사용되는 프레임(Frame)에 대한 정보를 포함하며, 이를 이용하여 메시지의 발신자, 수신자, 메시지 타입 등의 정보를 설정하거나 읽어올 수 있다.
                                                                      //                                STOMP 메시지에 사용될 헤더의 추가, 수정, 삭제, 조회 등을 할 수 있다.
        // 2. SimpMessageHeaderAccessor를 사용하여 "metaIdx"라는 이름의 헤더 정보를 추출해, metaIdx 변수에 방 번호를 전달한다.
        String metaIdx = accessor.getFirstNativeHeader("metaIdx");
        // 3. SimpMessageHeaderAccessor를 사용하여 "writer"라는 이름의 헤더 정보를 추출해, writer 변수에 작성자를 전달한다.
        String writer = accessor.getFirstNativeHeader("writer");

        // 4. SimpMessageHeaderAccessor 클래스의 create() 메소드를 사용하여 headers 객체를 생성한다.
        //    SimpMessageHeaderAccessor - Spring 프레임워크의 메시지 핸들러에서 메시지의 헤더 정보를 읽거나 쓸 수 있도록 지원하는 클래스이다.
        //    create() - 새로운 SimpMessageHeaderAccessor 객체를 생성하고 반환한다.
        //    이렇게 생성된 SimpMessageHeaderAccessor 객체를 통해 메시지 헤더에 접근하여 정보를 설정하거나 조회할 수 있다.
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        // 5. setLeaveMutable(true) - SimpMessageHeaderAccessor 객체가 생성될 때 생성되는 내부 MessageHeaders 객체를 변경 가능하게 설정하는 메소드이다.
        //                            MessageHeaders 객체에는 메시지 헤더와 관련된 정보가 들어있으며, 이 정보들을 변경할 수 있도록 하기 위해 setLeaveMutable() 메소드를 호출하여 변경 가능하게 설정한다.
        //                            이후 SimpMessageHeaderAccessor 객체를 사용하여 메시지 헤더에 정보를 추가하거나 제거할 수 있다.
        headers.setLeaveMutable(true);
        // setNativeHeader("key", "value") - SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header를 설정하는 메소드로, 이 메소드를 사용하기 위해서는 key와 value를 인자로 전달해주어야 한다.
        //                                   이렇게 key와 value로 설정된 native header는 STOMP 프로토콜을 사용하는 메시지 전송 시에 추가적인 헤더 정보를 담을 때 사용된다.
        // 6. setNativeHeader("type", "record") - SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "type"을 "record"로 설정하는 코드이다.
        //                                        이 코드를 통해 "type" header를 "record"로 설정하여, 클라이언트 측에서 해당 메시지를 녹음 메시지로 인식하고 처리할 수 있도록 한다.
        headers.setNativeHeader("type", "record");
        // 7. setNativeHeader("type", "record") - SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "writer"을 3에서 헤더 정보를 추출해 전달받은 작성자로 설정하는 코드이다.
        //                                        이 코드를 통해 "writer" header를 작성자로 설정하여, 클라이언트 측에서 해당 메시지를 누가 보냈는지 인식하고 처리할 수 있도록 한다.
        headers.setNativeHeader("writer", writer);

        // 8. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 2에서 헤더 정보를 추출해 전달받은 방 번호가 병합된다.
        //    "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
        template.convertAndSend("/sub/meta/studyroom/" + metaIdx, recordFile, headers.getMessageHeaders());
    }

    // 스터디룸 방장 위임
    @MessageMapping(value = "/meta/studyroom/delegatemaster")
    public void delegateMasterStudyRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 방장 위임 정보들을 DTO로 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO 값 중 방 제목과 작성자(위임 전 방장)와 방장(위임 후 방장)을 가져와 방장 위임 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
        message.setMessage(message.getMetaTitle() + "방에 방장이 " + message.getWriter() + "님에서 " + message.getMaster() + "님으로 변경되었습니다.");
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
        template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
    }

    // 스터디룸 강퇴
    @MessageMapping(value = "/meta/studyroom/kickout")
    public void kickOutStudyRoom(ChatMessageDTO message) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 강퇴 정보들을 DTO로 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO 값 중 작성자(강퇴자)와 방 제목을 가져와 강퇴 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
        message.setMessage(message.getWriter() + "님은 " + message.getMetaTitle() + "방에서 강제 탈주 되셨습니다.");
        // 3. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다.
        message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
        // 4. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
        template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);

        // 5. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(message.getMetaIdx());
        // 6. 6에서 가져온 Map에서, 1에서 파라미터로 받아온 DTO 값 중 작성자(강퇴자) 키에 해당하는 List를 제거한다.
        metaCanvasMap.remove(message.getWriter());
        // 7. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 6에서 가져온 Map을 JSON 문자열로 변환한다.
        String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
        // 8. 8에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
        message.setExit(metaCanvasJson);
        // 9. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
        template.convertAndSend("/sub/meta/studyroom/canvas/" + message.getMetaIdx(), message);
    }

    // 스터디룸 퇴장
    @MessageMapping(value = "/meta/studyroom/exit")
    public void exitStudyRoom(ChatMessageDTO message) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 퇴장 정보들을 DTO로 받아온다.
        // 2. metaController에서 재입장(새로고침) 체크용 Map에 추가한 작성자(퇴장자) 키에 해당하는 재입장(새로고침) 체크 값을 가져온다.
        Long reEnterCheck = metaController.reEnterCheck.get(message.getWriter());
        // 3. 2에서 가져온 재입장(새로고침) 체크 값이 존재하는지 체크한다.
        // 3-1. 재입장(새로고침) 체크 값이 존재하는 경우 - 재입장(새로고침)
        if ( reEnterCheck != null ) {
            // 3-1-1. metaController에서 재입장(새로고침) 체크용 Map에 추가한 작성자(퇴장자) 키에 해당하는 재입장(새로고침) 체크 값을 제거한다.
            metaController.reEnterCheck.remove(message.getWriter());
            // 3-1-2. 퇴장한 것이 아니기에 더 이상 작업할 것이 없다.
        // 3-2. 재입장(새로고침) 체크 값이 존재하지 않는 경우 - 퇴장
        } else {
            // 4. 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자)와 방 제목을 가져와 퇴장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
            message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에서 탈주하였습니다.");
            // 5. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다.
            message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
            // 6. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 Map을 가져온다.
            Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(message.getMetaIdx());
            // 7. 6에서 가져온 Map에서, 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자) 키에 해당하는 List를 제거한다.
            metaCanvasMap.remove(message.getWriter());
            // 8. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 9에서 가져온 Map을 JSON 문자열로 변환한다.
            String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
            // 9. 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임이 존재하는지 체크한다.
            // 9-1. 방장 닉네임이 존재하지 않는 경우
            if ( message.getMaster() == null ) {
                // 9-1-1. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
                template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
                // 9-1-2. 8에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
                message.setExit(metaCanvasJson);
                // 9-1-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
                template.convertAndSend("/sub/meta/studyroom/canvas/" + message.getMetaIdx(), message);
            // 9-2. 방장 닉네임이 존재하는 경우
            } else {
                // 10. 참여중인 인원이 0명인지 체크한다.
                // 10-1. 참여중인 인원이 0명인 경우 - 방 삭제
                if ( message.getMetaRecruitingPersonnel() == 0 ) {
                    // 10-1-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 가지고 metaController에 방 삭제 메소드를 호출하여 방을 퇴장하고 삭제한다.
                    metaController.deleteRoomMaster(message.getMetaIdx());
                // 10-2. 참여중인 인원이 0명이 아닌 경우 - 방 유지
                } else {
                    // 10-2-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 키로 사용하고, 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임을 값으로 사용하여, 위에서 생성한 방장 재입장 체크용 Map에 추가한다.
                    boomMetaRoom.put(message.getMetaIdx(), message.getMaster());
                    // 10-2-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                    //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                    //         "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
                    template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
                    // 10-2-3. 11에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
                    message.setExit(metaCanvasJson);
                    // 10-2-4. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                    //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                    //         "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
                    template.convertAndSend("/sub/meta/studyroom/canvas/" + message.getMetaIdx(), message);
                    try {
                        // 10-2-5. 퇴장 메시지를 전송하고 난 후 1분 대기하여 방장이 완전한 퇴장인지 다시 재입장 하는지 체크한다.
                        Thread.sleep(60000);
                        // 10-2-6. 10-2-1에서 방장 재입장 체크용 Map에 추가한 키에 해당하는 값을 삭제한다.
                        boomMetaRoom.remove(message.getMetaIdx());
                    } catch (InterruptedException ex) { // 스레드를 중지하거나 중단시킬 때 발생할 수 있는 예외가 발생한 경우 catch문이 실행된다.
                        // InterruptedException - 스레드가 sleep() 상태에서 interrupt() 메소드 호출로 깨어나서 예외를 발생시킨 경우 발생한다.
                        //                        스레드가 인터럽트되면 스레드가 일시 중단되고, 인터럽트 상태 플래그가 true로 설정되는데,
                        //                        이 경우, Thread.currentThread().interrupt() 메소드를 호출하여 인터럽트 상태 플래그를 재설정해준다.
                        // Thread.currentThread().interrupt() - 스레드를 강제 종료시키는 것이 아니라, 인터럽트 플래그를 설정함으로써 스레드가 계속 실행되지 않도록 만드는 메소드이다.
                        Thread.currentThread().interrupt(); // 현재 스레드에 인터럽트 플래그를 설정하여, 스레드가 계속 실행되지 않도록 만든다.
                    } // try - catch
                } // message.getMetaRecruitingPersonnel() == 0
            } // message.getMaster() == null
        } // reEnterCheck == null
    } // exitStudyRoom(ChatMessageDTO message)

//    // 스터디룸 퇴장 - 폐기
//    @MessageMapping(value = "/meta/studyroom/exit")
//    // Future - Future 인터페이스는 Java5부터 java.util.concurrency 패키지에서 비동기의 결과값을 받는 용도로 사용했지만 비동기의 결과값을 조합하거나, error를 핸들링할 수가 없었다.
//    // CompletionStage - Java 8에서 추가된 인터페이스 중 하나로, 비동기식 계산 결과를 다루기 위한 일종의 통합 API이다.
//    //                   CompletableFuture 클래스와 함께 사용되어, 비동기 작업을 수행하고 작업 결과를 처리하는 기능을 제공한다.
//    // CompletableFuture - Java 8에서 추가된 클래스 중 하나로, 비동기 작업을 수행하고 해당 작업의 결과를 처리하는 기능을 제공한다.
//    // <Void> - runAsync는 반환 값이 없으므로 Void 타입이다.
//    public CompletableFuture<Void> exitStudyRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 퇴장 정보들을 DTO로 받아온다.
//        // 2. 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자)와 방 제목을 가져와 퇴장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
//        message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에서 탈주하였습니다.");
//        // 3. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다.
//        message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
//
//        // 4. 1에서 파라미터로 받아온 DTO 값 중 방 번호와 퇴장을 의미하는 문자를 조합하여 키로 사용하고, 1에서 파라미터로 받아온 DTO를 값으로 사용하여, 위에서 생성한 새로고침 체크용 Map에 추가한다.
//        metaMessageMap.put(message.getMetaIdx() + "_exit", message);
//
//        // CompletableFuture.runAsync - 비동기적으로 실행되는 작업을 수행하는 CompletableFuture 객체를 반환한다.
//        // runAsync() - 파라미터로 Runnable 객체를 받으며, 이 객체의 run() 메소드 안에 비동기적으로 실행할 작업을 구현한다.
//        //              메소드는 즉시 리턴하며, 별도의 쓰레드에서 run() 메소드 안에 구현된 작업이 비동기적으로 실행된다.
//        // Runnable - 인자를 받지 않고, 리턴값도 없는 함수형 인터페이스이다.
//        //            run() 메소드를 하나만 가지고 있으며, 이 메소드에서 수행될 작업을 구현한다.
//        //            run() 메소드는 매개변수를 받지 않으며, 리턴값도 없다.
//        return CompletableFuture.runAsync(() -> {
//            try {
//                // 5. 퇴장 메시지를 전송하기 전에 0.3초 대기하여 퇴장인지 재입장(새로고침)인지 체크한다.
//                Thread.sleep(300);
//                // 6. 4에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 다시 가져온다.
//                ChatMessageDTO exitMessage = metaMessageMap.get(message.getMetaIdx() + "_exit");
//                // 7. 5에서 0.3초 대기한 후에도 6에서 가져온 DTO가 여전히 존재하는지 체크한다.
//                // 7-1. 퇴장 메시지가 존재하는 경우 - 퇴장
//                if ( exitMessage == null ) {
//                    // 7-1-1. 퇴장한 것이 아니기에 더 이상 작업할 것이 없다.
//                // 7-2. 퇴장 메시지가 존재하지 않는 경우 - 재입장(새로고침)
//                } else {
//                    // 8. 4에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 삭제한다.
//                    metaMessageMap.remove(message.getMetaIdx() + "_exit");
//                    // 9. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 Map을 가져온다.
//                    Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(message.getMetaIdx());
//                    // 10. 9에서 가져온 Map에서, 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자) 키에 해당하는 List를 제거한다.
//                    metaCanvasMap.remove(message.getWriter());
//                    // 11. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 9에서 가져온 Map을 JSON 문자열로 변환한다.
//                    String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
//                    // 13. 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임이 존재하는지 체크한다.
//                    // 13-1. 방장 닉네임이 존재하지 않는 경우
//                    if ( message.getMaster() == null ) {
//                        // 13-1-1. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                        //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                        //         "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
//                        template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
//                        // 13-1-2. 11에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
//                        message.setExit(metaCanvasJson);
//                        // 13-1-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                        //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                        //         "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
//                        template.convertAndSend("/sub/meta/studyroom/canvas/" + message.getMetaIdx(), message);
//                    // 13-2. 방장 닉네임이 존재하는 경우
//                    } else {
//                        // 14. 참여중인 인원이 0명인지 체크한다.
//                        // 14-1. 참여중인 인원이 0명인 경우 - 방 삭제
//                        if ( message.getMetaRecruitingPersonnel() == 0 ) {
//                            // 14-1-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 가지고 metaController에 방 삭제 메소드를 호출하여 방을 퇴장하고 삭제한다.
//                            metaController.deleteRoomMaster(message.getMetaIdx());
//                        // 14-2. 참여중인 인원이 0명이 아닌 경우 - 방 유지
//                        } else {
//                            // 14-2-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 키로 사용하고, 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임을 값으로 사용하여, 위에서 생성한 방장 재입장 체크용 Map에 추가한다.
//                            boomMetaRoom.put(message.getMetaIdx(), message.getMaster());
//                            // 14-2-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                            //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                            //         "/sub" + "/meta/studyroom/" + metaIdx = "/sub/meta/studyroom/1"
//                            template.convertAndSend("/sub/meta/studyroom/" + message.getMetaIdx(), message);
//                            // 14-2-3. 11에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
//                            message.setExit(metaCanvasJson);
//                            // 14-2-4. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                            //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                            //         "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
//                            template.convertAndSend("/sub/meta/studyroom/canvas/" + message.getMetaIdx(), message);
//                            // 14-2-5. 퇴장 메시지를 전송하고 난 후 1분 대기하여 방장이 완전한 퇴장인지 다시 재입장 하는지 체크한다.
//                            Thread.sleep(60000);
//                            // 14-2-6. 14-2-1에서 방장 재입장 체크용 Map에 추가한 키에 해당하는 값을 삭제한다.
//                            boomMetaRoom.remove(message.getMetaIdx());
//                        }
//                    }
//                }
//            } catch (InterruptedException e) { // 스레드를 중지하거나 중단시킬 때 발생할 수 있는 예외가 발생한 경우 catch문이 실행된다.
//                // InterruptedException - 스레드가 sleep() 상태에서 interrupt() 메소드 호출로 깨어나서 예외를 발생시킨 경우 발생한다.
//                //                        스레드가 인터럽트되면 스레드가 일시 중단되고, 인터럽트 상태 플래그가 true로 설정되는데,
//                //                        이 경우, Thread.currentThread().interrupt() 메소드를 호출하여 인터럽트 상태 플래그를 재설정해준다.
//                // Thread.currentThread().interrupt() - 스레드를 강제 종료시키는 것이 아니라, 인터럽트 플래그를 설정함으로써 스레드가 계속 실행되지 않도록 만드는 메소드이다.
//                Thread.currentThread().interrupt(); // 현재 스레드에 인터럽트 플래그를 설정하여, 스레드가 계속 실행되지 않도록 만든다.
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
    ////////////////////////////////////////////////// 스터디룸 캔버스 구역 //////////////////////////////////////////////////
    // 스터디룸 캔버스 첫 입장
    @MessageMapping(value = "/meta/studyroom/canvas/enter")
    public void canvasEnterStudyRoom(MetaCanvasDTO canvas) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 캐릭터 정보들을 DTO로 받아온다.
        // 2. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 캐릭터 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(canvas.getMetaIdx());
        // 3. 2에서 가져온 캐릭터 Map이 존재하는지 체크한다.
        // 3-1. Map이 존재하지 않는 경우
        if ( metaCanvasMap == null ) {
            // 3-1-1. 2에서 가져오는 캐릭터 Map을 생성한다.
            metaCanvasMap = new HashMap<>();
            // 3-1-2. 3-1-1에서 생성한 캐릭터 Map에 값으로 사용할 캐릭터 List를 생성한다.
            List<Object> metaCoordinateList = new ArrayList();
            // 3-1-3. 3-1-2에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 캐릭터 이름을 전달한다.
            metaCoordinateList.add(canvas.getCharacter());
            // 3-1-4. 3-1-2에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 x축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getX());
            // 3-1-5. 3-1-2에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 y축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getY());
            // 3-1-6. 1에서 파라미터로 받아온 DTO 값 중 닉네임을 키로 사용하고, 3-1-2에서 생성한 캐릭터 List를 값으로 사용하여, 3-1-1에서 생성한 캐릭터 Map에 추가한다.
            metaCanvasMap.put(canvas.getWriter(), metaCoordinateList);
            // 3-1-7. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 키로 사용하고, 3-1-6에서 캐릭터 List가 추가된 캐릭터 Map을 값으로 사용하여, 위에서 생성한 방 구분용 Map에 추가한다.
            metaRoomMap.put(canvas.getMetaIdx(), metaCanvasMap);
            // 3-1-8. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 3-1-1에서 생성한 캐릭터 Map을 JSON 문자열로 변환한다.
            String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
            // 3-1-9. 3-1-8에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
            canvas.setCharacters(metaCanvasJson);
            // 3-1-10. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
            //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
            //         "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
            template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas);
        // 3-2. Map이 존재하는 경우
        } else {
            // 3-2-1. 2에서 가져온 캐릭터 Map에 값으로 사용할 List를 생성한다.
            List<Object> metaCoordinateList = new ArrayList();
            // 3-2-2. 3-2-1에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 캐릭터 이름을 전달한다.
            metaCoordinateList.add(canvas.getCharacter());
            // 3-2-3. 3-2-1에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 x축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getX());
            // 3-2-4. 3-2-1에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 y축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getY());
            // 3-2-5. 1에서 파라미터로 받아온 DTO 값 중 닉네임을 키로 사용하고, 3-2-1에서 생성한 캐릭터 List를 값으로 사용하여, 2에서 가져온 Map에 추가한다.
            metaCanvasMap.put(canvas.getWriter(), metaCoordinateList);
            // 3-2-6. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 2에서 가져온 캐릭터 Map을 JSON 문자열로 변환한다.
            String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
            // 3-2-7. 3-2-6에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
            canvas.setCharacters(metaCanvasJson);
            // 3-2-8. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
            //        "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
            template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas);
        }
    }

    // 스터디룸 캔버스 첫 입장 이후 재입장 - 첫 입장 이후 모든 재입장은 이곳으로 들어온다.
    @MessageMapping(value = "/meta/studyroom/canvas/reenter")
    public void canvasReEnterStudyRoom(MetaCanvasDTO canvas) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 캐릭터 정보들을 DTO로 받아온다.
        // 2. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 캐릭터 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(canvas.getMetaIdx());
        // 3. 2에서 가져온 캐릭터 Map이 존재하는지 체크한다.
        // 3-1. Map이 존재하지 않는 경우 - 재입장 에러
        if ( metaCanvasMap == null ) {
            // 3-1-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "reErr"로 변경한다. - 재입장 에러 값
            canvas.setType("reErr");
            // 3-1-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
            //        "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
            template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas);
        // 3-2. Map이 존재하는 경우 - 재입장(새로고침)
        } else {
            // 4. 2에서 가져온 캐릭터 Map에서, 1에서 파라미터로 받아온 DTO 값 중 닉네임 키에 해당하는 캐릭터 List를 가져온다.
            List<Object> metaCoordinateList = metaCanvasMap.get(canvas.getWriter());
            // 5. 4에서 가져온 캐릭터 List가 존재하는지 체크한다.
            // 5-1. List가 존재하지 않는 경우 - 재입장 에러
            if (metaCoordinateList == null) {
                // 5-1-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "reErr"로 변경한다. - 재입장 에러 값
                canvas.setType("reErr");
                // 5-1-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
                template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas);
            // 5-2. List가 존재하는 경우 - 재입장(새로고침)
            } else {
                // 5-2-1. 4에서 가져온 캐릭터 List 값 중 x좌표 값을, 1에서 파라미터로 받아온 DTO 값 중 x좌표 값으로 갱신한다.
                metaCoordinateList.set(1, canvas.getX());
                // 5-2-2. 4에서 가져온 캐릭터 List 값 중 y좌표 값을, 1에서 파라미터로 받아온 DTO 값 중 y좌표 값으로 갱신한다.
                metaCoordinateList.set(2, canvas.getY());
                // 5-2-3. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 2에서 가져온 캐릭터 Map을 JSON 문자열로 변환한다.
                String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
                // 5-2-4. 5-2-3에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
                canvas.setCharacters(metaCanvasJson);
                // 5-2-5. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
                template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas);
            }
        }
    }

    // 스터디룸 캔버스 캐릭터 이동 좌표 변경 - RabbitMQ 사용
    @MessageMapping(value = "/meta/studyroom/canvas/move")
    public void canvasMoveStudyRoom(MetaCanvasDTO canvas) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 캐릭터 정보들을 DTO로 받아온다.
        // 2. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 캐릭터 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(canvas.getMetaIdx());
        // 3. 2에서 가져온 캐릭터 Map에서, 1에서 파라미터로 받아온 DTO 값 중 닉네임 키에 해당하는 캐릭터 List를 가져온다.
        List<Object> metaCoordinateList = metaCanvasMap.get(canvas.getWriter());
        // 4. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 이동 분기를 결정한다.
        switch( canvas.getType() ) {
            // 4-1. 왼쪽으로 이동
            case "left":
                // 왼쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                // 왼쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-1-1. 3에서 가져온 캐릭터 List 값 중 x좌표 값을, 5 뺀 값으로 갱신한다.
                    metaCoordinateList.set(1, (int) metaCoordinateList.get(1) - 5);
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                }
            // 4-2. 위로 이동
            case "top":
                // 위쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                // 위쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-2-1. 3에서 가져온 캐릭터 List 값 중 y좌표 값을, 5 뺀 값으로 갱신한다.
                    metaCoordinateList.set(2, (int) metaCoordinateList.get(2) - 5);
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                }
            // 4-3. 오른쪽으로 이동
            case "right":
                // 오른쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                // 오른쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-3-1. 3에서 가져온 캐릭터 List 값 중 x좌표 값을, 5 더한 값으로 갱신한다.
                    metaCoordinateList.set(1, (int) metaCoordinateList.get(1) + 5);
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                }
            // 4-4. 아래로 이동
            case "bottom":
                // 아래쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                // 아래쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-4-1. 3에서 가져온 캐릭터 List 값 중 y좌표 값을, 5 더한 값으로 갱신한다.
                    metaCoordinateList.set(2, (int) metaCoordinateList.get(2) + 5);
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                }
        }
        // 5. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 2에서 가져온 캐릭터 Map을 JSON 문자열로 변환한다.
        String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
        // 6. 5에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
        canvas.setCharacters(metaCanvasJson);

//        // 7번과 8번은 RabbitMQConfig에서 미리 메시지 변환기를 만들어 설정해놨기에 사용할 필요가 없다.
//        // 7. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 1에서 파라미터로 받아온 DTO를 JSON 문자열로 변환한다.
//        String json = objectMapper.writeValueAsString(canvas);
//        // 8. Spring AMQP의 MessageBuilder 클래스를 사용하여 AMQP 메시지를 생성한다.
//        //    MessageBuilder.withBody(json.getBytes()) - 7에서 변환한 JSON 문자열을 byte 배열로 변환하여 AMQP 메시지의 body에 전달한다.
//        //    .setContentType("application/json") - 생성된 AMQP 메시지의 contentType을 "application/json"으로 설정한다.
//        //                                        - 이는 메시지의 body가 어떤 형식인지를 나타내는 값이다.
//        //    .build() - 설정이 완료된 AMQP 메시지를 빌드하여 반환한다.
//        Message message = MessageBuilder.withBody(json.getBytes())
//                                        .setContentType("application/json")
//                                        .build();

        // 9. 위에서 @Autowired로 생성한 RabbitTemplate을 사용하여 8에서 생성한 메시지를 전송한다.
        //    첫 번째 인자는 메시지를 발송할 Exchange 이름으로, 메시지를 받아서 어느 큐로 보낼지 결정하는 역할을 한다.
        //    두 번째 인자는 메시지를 전송할 Routing Key 이름으로, Exchange에서 메시지를 받아서 어느 큐로 보낼지 결정하는 규칙을 나타내는 값이다.
        //    세 번째 인자는 전송할 메시지 객체이다.
        //    convertAndSend() - 인자로 받은 메시지를 RabbitMQ Broker에 전송한다.
        rabbitTemplate.convertAndSend("StudyRoomExchange", "StudyRoomRoutingKey", canvas);
//        template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas);
    }

    // 스터디룸 캔버스 캐릭터 이동 좌표 전달 - RabbitMQ 사용
    // @RabbitListener(queues = "StudyRoomQueue") - RabbitMQ 메시지 큐의 "StudyRoomQueue"라는 큐를 대상으로 메시지를 수신하는 리스너 함수임을 선언한다.
    // ackMode = "MANUAL" - ackMode는 수신한 메시지의 처리가 완료되면 RabbitMQ에 알려주는 방식을 설정하는 것으로,
    //                      "MANUAL"로 설정하면  메시지를 수신한 후 명시적으로 channel.basicAck()를 호출하여 메시지를 처리 완료했다는 신호를 보내야 한다.
    @RabbitListener(queues = "StudyRoomQueue", ackMode = "MANUAL")
    public void receiveMessageStudyRoom(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) { // 1. RabbitMQ로부터 수신된 메시지를 파라미터로 받아온다.
                                                                                                                        //    Channel - 메시지 큐와 통신할 수 있는 채널이다.
                                                                                                                        //    @Header(AmqpHeaders.DELIVERY_TAG) - 메시지를 수신할 때 해당 메시지의 "delivery tag" 값을 가져오는 역할을 한다.
                                                                                                                        //                                        "delivery tag"는 RabbitMQ가 메시지의 유일성을 보장하기 위해 각 메시지에 할당한 일련번호이다.
        try {
//            // 2번은 RabbitMQConfig에서 미리 메시지 변환기를 만들어 설정해놨기에 사용할 필요가 없다.
//            // 2. 1에서 파라미터로 받아온 메시지를 UTF-8로 인코딩하여 String 형태로 변환한다.
//            //    message.getBody() - RabbitMQ로부터 수신된 메시지의 body를 바이트 배열 형태로 반환한다.
//            //    new String(byte[] bytes, String charset) - 생성자를 사용하여 바이트 배열을 문자열로 변환하며,
//            //                                               이 때 charset 인자에는 인코딩 방식(UTF-8)을 지정해주어야 한다.
//            String json = new String(message.getBody(), "UTF-8");

            // 3. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 1에서 파라미터로 받아온 JSON 형식의 메시지를 DTO로 변환한다.
            //    readValue - JSON 문자열을 Java 객체로 역직렬화하는 메소드로,
            //                첫 번째 인자로 역직렬화할 대상인 JSON 문자열을,
            //                두 번째 인자로 역직렬화할 타입인 Java 객체의 클래스를 전달받는다.
            MetaCanvasDTO canvas = objectMapper.readValue(message.getBody(), MetaCanvasDTO.class);
            // 4. 메시지가 잘 처리됬는지 체크한다.
            // 4-1. 메시지가 잘 처리됬을 경우
            //      RabbitMQ는 완료한 메시지를 삭제하고, 큐에서 제거한다.
            //      basicAck - RabbitMQ에 메시지 처리 완료를 알리는 메소드이다.
            channel.basicAck(tag, false);
            // 5. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 3에서 변환한 DTO를 다시 전달한다.
            //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 3에서 변환한 DTO 값 중 방 번호가 병합된다.
            //    "/sub" + "/meta/studyroom/canvas/" + metaIdx = "/sub/meta/studyroom/canvas/1"
            template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas);

//            // 메시지 헤더 설정 - 헤더에 추가할 것이 있을 경우 사용한다.
//            SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
//            headers.setLeaveMutable(true);
//            headers.setNativeHeader("clear-cache", "true");
//            template.convertAndSend("/sub/meta/studyroom/canvas/" + canvas.getMetaIdx(), canvas, headers.getMessageHeaders());
        } catch (Exception e) {
            try {
                // 4-2. 메시지가 잘 처리되지 않았을 경우
                //      RabbitMQ는 실패한 메시지를 다시 큐로 보내고, 3번째 파라미터인 requeue를 true로 설정하여 재처리한다.
                //      basicNack - RabbitMQ에 메시지 처리 실패를 알리는 메소드이다.
                channel.basicNack(tag, false, true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
/////////////////////////////////////////////////////// 카페룸 구역 ///////////////////////////////////////////////////////
    // 카페룸 첫 입장
    @MessageMapping(value = "/meta/caferoom/enter")
    public void enterCafeRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 첫 입장 정보들을 DTO로 받아온다.
        // 2. 먼저 위에서 생성한 방장 재입장 체크용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 값이 존재하는지 체크한다.
        // 2-1. Map에서 가져온 값이 존재하는 경우 - 방장 퇴장 후 1분 이내 유저 입장
        if ( boomMetaRoom.get(message.getMetaIdx()) != null ) {
            // 2-1-1. 그 다음 Map에서 가져온 값이 작성자와 같을 경우 - 방장 퇴장 후 1분 이내 방장 재입장
            if ( boomMetaRoom.get(message.getMetaIdx()).equals(message.getWriter()) ) {
                // 2-1-1-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "cancel"로 변경한다. - 방 삭제 취소 값
                message.setType("cancel");
                // 2-1-1-2. 퇴장 메소드에서 Map에 추가한 키에 해당하는 값을 삭제한다.
                boomMetaRoom.remove(message.getMetaIdx());
            }
        }
//        // 2-2. Map에서 가져온 값이 존재하지 않는 경우 - 방장 퇴장 전 입장
//        // 3. 1에서 파라미터로 받아온 DTO 값 중 에러 체크 값을 가져와 재입장 에러 값이 존재하는지 체크한다.
//        // 3-1. 재입장 에러 값이 존재하는 경우 - 재입장 메시지를 작성한다.
//        if ( message.getErr() != null && message.getErr().equals("reErr") ) {
//            // 3-1-1. 1에서 파라미터로 받아온 DTO 값 중 작성자(재입장자)와 방 제목을 가져와 재입장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
//            message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에 재입장하였습니다.");
//        // 3-2. 재입장 에러 값이 존재하지 않는 경우 - 첫 입장 메시지 작성한다.
//        } else {
//            // 3-2-1. 1에서 파라미터로 받아온 DTO 값 중 작성자(첫 입장자)와 방 제목을 가져와 입장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
//            message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에 입장하였습니다.");
//        }
        // 3. 1에서 파라미터로 받아온 DTO 값 중 작성자(첫 입장자)와 방 제목을 가져와 입장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
        message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에 입장하였습니다.");
        // 4. 1에서 파라미터로 받아온 DTO 값 중 작성자를 가져와 DTO 값 중 참가자에 setter를 통해 전달한다.
        message.setParticipant(message.getWriter());
        // 5. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
        template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
    }

    // 카페룸 첫 입장 이후 재입장 - 첫 입장 이후 모든 재입장은 이곳으로 들어온다.
    @MessageMapping(value = "/meta/caferoom/reenter")
    public void reEnterCafeRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 재입장(새로고침) 정보들을 DTO로 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO 값 중 작성자(재입장자)를 가져와 DTO 값 중 참가자에 setter를 통해 전달한다.
        message.setParticipant(message.getWriter());
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
        template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
    }

//    // 카페룸 첫 입장 이후 재입장 - 첫 입장 이후 모든 재입장은 이곳으로 들어온다. - 폐기
//    @MessageMapping(value = "/meta/caferoom/reenter")
//    public void reEnterCafeRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 재입장(새로고침) 정보들을 DTO로 받아온다.
//        // 2. 이전 퇴장 메소드에서 Map에 추가한 키에 해당하는 DTO를 다시 가져온다.
//        ChatMessageDTO exitMessage = metaMessageMap.get(message.getMetaIdx() + "_exit");
//        // 3. 2에서 가져온 DTO가 여전히 존재하는지 체크한다.
//        // 3-1. 퇴장 메시지가 존재하는 경우 - 재입장(새로고침)
//        if ( exitMessage != null ) {
//            // 3-1-1. 퇴장 메소드에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 삭제한다.
//            metaMessageMap.remove(message.getMetaIdx() + "_exit");
//            // 3-1-2. 1에서 파라미터로 받아온 DTO 값 중 작성자(재입장자)를 가져와 DTO 값 중 참가자에 setter를 통해 전달한다.
//            message.setParticipant(message.getWriter());
//            // 3-1-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//            //        "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
//            template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
//        // 3-2. 퇴장 메시지가 존재하지 않는 경우 - 0.3초가 넘는 장시간의 새로고침 에러로 인한 퇴장 처리 후 재입장된 것으로,
//        //                                 이는 아직 퇴장한 것이 아닌데 퇴장 처리가 되었으므로 다시 입장 처리를 해준다.
//        } else {
//            // 3-2-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "reErr"로 변경한다. - 재입장 에러 값
//            message.setType("reErr");
//            // 3-2-2. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다. - 재입장 에러로 퇴장
//            message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
//            // 3-2-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//            //        "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
//            template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
//        }
//    }

    // 카페룸 채팅
    @MessageMapping(value = "/meta/caferoom/message")
    public void messageCafeRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 채팅 정보들을 DTO로 받아온다.
        // 2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 메시지 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
        template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
    }

    // 카페룸 녹음
    @MessageMapping(value = "/meta/caferoom/record")
    public void recordCafeRoom(@Payload String recordFile,
                                SimpMessageHeaderAccessor accessor) { // 1. 클라이언트로부터 전송된 녹음된 오디오 데이터를 @Payload로 받아온다.
                                                                      //    @Payload - Spring 프레임워크에서 메시지 페이로드를 메소드 파라미터와 매핑할 때 사용되는 어노테이션이다.
                                                                      //               @Payload가 붙은 파라미터는 메시지 페이로드 자체를 나타내며, Spring은 메시지 변환기(MessageConverter)를 사용하여 해당 객체로 변환한다.
                                                                      //               일반적으로 Spring WebSocket이나 STOMP 메시지를 처리하는 메서드에서 많이 사용되며,
                                                                      //               이 어노테이션을 사용하면 개발자가 메시지 변환에 필요한 코드를 작성하지 않아도 되므로 코드 간결성과 생산성을 높일 수 있다.
                                                                      //    SimpMessageHeaderAccessor - Spring 프레임워크의 MessageHeaderAccessor 인터페이스를 구현한 클래스로, STOMP 메시지의 헤더 정보를 쉽게 조작하고 접근할 수 있게 해준다.
                                                                      //                                STOMP 프로토콜에서 사용되는 프레임(Frame)에 대한 정보를 포함하며, 이를 이용하여 메시지의 발신자, 수신자, 메시지 타입 등의 정보를 설정하거나 읽어올 수 있다.
                                                                      //                                STOMP 메시지에 사용될 헤더의 추가, 수정, 삭제, 조회 등을 할 수 있다.
        // 2. SimpMessageHeaderAccessor를 사용하여 "metaIdx"라는 이름의 헤더 정보를 추출해, metaIdx 변수에 방 번호를 전달한다.
        String metaIdx = accessor.getFirstNativeHeader("metaIdx");
        // 3. SimpMessageHeaderAccessor를 사용하여 "writer"라는 이름의 헤더 정보를 추출해, writer 변수에 작성자를 전달한다.
        String writer = accessor.getFirstNativeHeader("writer");

        // 4. SimpMessageHeaderAccessor 클래스의 create() 메소드를 사용하여 headers 객체를 생성한다.
        //    SimpMessageHeaderAccessor - Spring 프레임워크의 메시지 핸들러에서 메시지의 헤더 정보를 읽거나 쓸 수 있도록 지원하는 클래스이다.
        //    create() - 새로운 SimpMessageHeaderAccessor 객체를 생성하고 반환한다.
        //    이렇게 생성된 SimpMessageHeaderAccessor 객체를 통해 메시지 헤더에 접근하여 정보를 설정하거나 조회할 수 있다.
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        // 5. setLeaveMutable(true) - SimpMessageHeaderAccessor 객체가 생성될 때 생성되는 내부 MessageHeaders 객체를 변경 가능하게 설정하는 메소드이다.
        //                            MessageHeaders 객체에는 메시지 헤더와 관련된 정보가 들어있으며, 이 정보들을 변경할 수 있도록 하기 위해 setLeaveMutable() 메소드를 호출하여 변경 가능하게 설정한다.
        //                            이후 SimpMessageHeaderAccessor 객체를 사용하여 메시지 헤더에 정보를 추가하거나 제거할 수 있다.
        headers.setLeaveMutable(true);
        // setNativeHeader("key", "value") - SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header를 설정하는 메소드로, 이 메소드를 사용하기 위해서는 key와 value를 인자로 전달해주어야 한다.
        //                                   이렇게 key와 value로 설정된 native header는 STOMP 프로토콜을 사용하는 메시지 전송 시에 추가적인 헤더 정보를 담을 때 사용된다.
        // 6. setNativeHeader("type", "record") - SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "type"을 "record"로 설정하는 코드이다.
        //                                        이 코드를 통해 "type" header를 "record"로 설정하여, 클라이언트 측에서 해당 메시지를 녹음 메시지로 인식하고 처리할 수 있도록 한다.
        headers.setNativeHeader("type", "record");
        // 7. setNativeHeader("type", "record") - SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "writer"을 3에서 헤더 정보를 추출해 전달받은 작성자로 설정하는 코드이다.
        //                                        이 코드를 통해 "writer" header를 작성자로 설정하여, 클라이언트 측에서 해당 메시지를 누가 보냈는지 인식하고 처리할 수 있도록 한다.
        headers.setNativeHeader("writer", writer);

        // 8. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 2에서 헤더 정보를 추출해 전달받은 방 번호가 병합된다.
        //    "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
        template.convertAndSend("/sub/meta/caferoom/" + metaIdx, recordFile, headers.getMessageHeaders());
    }

    // 카페룸 방장 위임
    @MessageMapping(value = "/meta/caferoom/delegatemaster")
    public void delegateMasterCafeRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 방장 위임 정보들을 DTO로 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO 값 중 방 제목과 작성자(위임 전 방장)와 방장(위임 후 방장)을 가져와 방장 위임 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
        message.setMessage(message.getMetaTitle() + "방에 방장이 " + message.getWriter() + "님에서 " + message.getMaster() + "님으로 변경되었습니다.");
        // 3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
        template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
    }

    // 카페룸 강퇴
    @MessageMapping(value = "/meta/caferoom/kickout")
    public void kickOutCafeRoom(ChatMessageDTO message) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 강퇴 정보들을 DTO로 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO 값 중 작성자(강퇴자)와 방 제목을 가져와 강퇴 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
        message.setMessage(message.getWriter() + "님은 " + message.getMetaTitle() + "방에서 강제 탈주 되셨습니다.");
        // 3. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다.
        message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
        // 4. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
        template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);

        // 5. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(message.getMetaIdx());
        // 6. 6에서 가져온 Map에서, 1에서 파라미터로 받아온 DTO 값 중 작성자(강퇴자) 키에 해당하는 List를 제거한다.
        metaCanvasMap.remove(message.getWriter());
        // 7. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 6에서 가져온 Map을 JSON 문자열로 변환한다.
        String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
        // 8. 8에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
        message.setExit(metaCanvasJson);
        // 9. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
        //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
        //    "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
        template.convertAndSend("/sub/meta/caferoom/canvas/" + message.getMetaIdx(), message);
    }

    // 카페룸 퇴장
    @MessageMapping(value = "/meta/caferoom/exit")
    public void exitCafeRoom(ChatMessageDTO message) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 퇴장 정보들을 DTO로 받아온다.
        // 2. metaController에서 재입장(새로고침) 체크용 Map에 추가한 작성자(퇴장자) 키에 해당하는 재입장(새로고침) 체크 값을 가져온다.
        Long reEnterCheck = metaController.reEnterCheck.get(message.getWriter());
        // 3. 2에서 가져온 재입장(새로고침) 체크 값이 존재하는지 체크한다.
        // 3-1. 재입장(새로고침) 체크 값이 존재하는 경우 - 재입장(새로고침)
        if ( reEnterCheck != null ) {
            // 3-1-1. metaController에서 재입장(새로고침) 체크용 Map에 추가한 작성자(퇴장자) 키에 해당하는 재입장(새로고침) 체크 값을 제거한다.
            metaController.reEnterCheck.remove(message.getWriter());
            // 3-1-2. 퇴장한 것이 아니기에 더 이상 작업할 것이 없다.
        // 3-2. 재입장(새로고침) 체크 값이 존재하지 않는 경우 - 퇴장
        } else {
            // 4. 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자)와 방 제목을 가져와 퇴장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
            message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에서 탈주하였습니다.");
            // 5. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다.
            message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
            // 6. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 Map을 가져온다.
            Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(message.getMetaIdx());
            // 7. 6에서 가져온 Map에서, 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자) 키에 해당하는 List를 제거한다.
            metaCanvasMap.remove(message.getWriter());
            // 8. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 9에서 가져온 Map을 JSON 문자열로 변환한다.
            String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
            // 9. 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임이 존재하는지 체크한다.
            // 9-1. 방장 닉네임이 존재하지 않는 경우
            if ( message.getMaster() == null ) {
                // 9-1-1. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
                template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
                // 9-1-2. 8에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
                message.setExit(metaCanvasJson);
                // 9-1-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
                template.convertAndSend("/sub/meta/caferoom/canvas/" + message.getMetaIdx(), message);
            // 9-2. 방장 닉네임이 존재하는 경우
            } else {
                // 10. 참여중인 인원이 0명인지 체크한다.
                // 10-1. 참여중인 인원이 0명인 경우 - 방 삭제
                if ( message.getMetaRecruitingPersonnel() == 0 ) {
                    // 10-1-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 가지고 metaController에 방 삭제 메소드를 호출하여 방을 퇴장하고 삭제한다.
                    metaController.deleteRoomMaster(message.getMetaIdx());
                // 10-2. 참여중인 인원이 0명이 아닌 경우 - 방 유지
                } else {
                    // 10-2-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 키로 사용하고, 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임을 값으로 사용하여, 위에서 생성한 방장 재입장 체크용 Map에 추가한다.
                    boomMetaRoom.put(message.getMetaIdx(), message.getMaster());
                    // 10-2-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                    //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                    //         "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
                    template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
                    // 10-2-3. 11에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
                    message.setExit(metaCanvasJson);
                    // 10-2-4. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                    //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                    //         "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
                    template.convertAndSend("/sub/meta/caferoom/canvas/" + message.getMetaIdx(), message);
                    try {
                        // 10-2-5. 퇴장 메시지를 전송하고 난 후 1분 대기하여 방장이 완전한 퇴장인지 다시 재입장 하는지 체크한다.
                        Thread.sleep(60000);
                        // 10-2-6. 10-2-1에서 방장 재입장 체크용 Map에 추가한 키에 해당하는 값을 삭제한다.
                        boomMetaRoom.remove(message.getMetaIdx());
                    } catch (InterruptedException ex) { // 스레드를 중지하거나 중단시킬 때 발생할 수 있는 예외가 발생한 경우 catch문이 실행된다.
                        // InterruptedException - 스레드가 sleep() 상태에서 interrupt() 메소드 호출로 깨어나서 예외를 발생시킨 경우 발생한다.
                        //                        스레드가 인터럽트되면 스레드가 일시 중단되고, 인터럽트 상태 플래그가 true로 설정되는데,
                        //                        이 경우, Thread.currentThread().interrupt() 메소드를 호출하여 인터럽트 상태 플래그를 재설정해준다.
                        // Thread.currentThread().interrupt() - 스레드를 강제 종료시키는 것이 아니라, 인터럽트 플래그를 설정함으로써 스레드가 계속 실행되지 않도록 만드는 메소드이다.
                        Thread.currentThread().interrupt(); // 현재 스레드에 인터럽트 플래그를 설정하여, 스레드가 계속 실행되지 않도록 만든다.
                    } // try - catch
                } // message.getMetaRecruitingPersonnel() == 0
            } // message.getMaster() == null
        } // reEnterCheck == null
    } // exitCafeRoom(ChatMessageDTO message)

//    // 카페룸 퇴장 - 폐기
//    @MessageMapping(value = "/meta/caferoom/exit")
//    // Future - Future 인터페이스는 Java5부터 java.util.concurrency 패키지에서 비동기의 결과값을 받는 용도로 사용했지만 비동기의 결과값을 조합하거나, error를 핸들링할 수가 없었다.
//    // CompletionStage - Java 8에서 추가된 인터페이스 중 하나로, 비동기식 계산 결과를 다루기 위한 일종의 통합 API이다.
//    //                   CompletableFuture 클래스와 함께 사용되어, 비동기 작업을 수행하고 작업 결과를 처리하는 기능을 제공한다.
//    // CompletableFuture - Java 8에서 추가된 클래스 중 하나로, 비동기 작업을 수행하고 해당 작업의 결과를 처리하는 기능을 제공한다.
//    // <Void> - runAsync는 반환 값이 없으므로 Void 타입이다.
//    public CompletableFuture<Void> exitCafeRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 퇴장 정보들을 DTO로 받아온다.
//        // 2. 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자)와 방 제목을 가져와 퇴장 메시지를 작성하여 DTO 값 중 메시지에 setter를 통해 전달한다.
//        message.setMessage(message.getWriter() + "님이 " + message.getMetaTitle() + "방에서 탈주하였습니다.");
//        // 3. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다.
//        message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
//
//        // 4. 1에서 파라미터로 받아온 DTO 값 중 방 번호와 퇴장을 의미하는 문자를 조합하여 키로 사용하고, 1에서 파라미터로 받아온 DTO를 값으로 사용하여, 위에서 생성한 새로고침 체크용 Map에 추가한다.
//        metaMessageMap.put(message.getMetaIdx() + "_exit", message);
//
//        // CompletableFuture.runAsync - 비동기적으로 실행되는 작업을 수행하는 CompletableFuture 객체를 반환한다.
//        // runAsync() - 파라미터로 Runnable 객체를 받으며, 이 객체의 run() 메소드 안에 비동기적으로 실행할 작업을 구현한다.
//        //              메소드는 즉시 리턴하며, 별도의 쓰레드에서 run() 메소드 안에 구현된 작업이 비동기적으로 실행된다.
//        // Runnable - 인자를 받지 않고, 리턴값도 없는 함수형 인터페이스이다.
//        //            run() 메소드를 하나만 가지고 있으며, 이 메소드에서 수행될 작업을 구현한다.
//        //            run() 메소드는 매개변수를 받지 않으며, 리턴값도 없다.
//        return CompletableFuture.runAsync(() -> {
//            try {
//                // 5. 퇴장 메시지를 전송하기 전에 0.3초 대기하여 퇴장인지 재입장(새로고침)인지 체크한다.
//                Thread.sleep(300);
//                // 6. 4에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 다시 가져온다.
//                ChatMessageDTO exitMessage = metaMessageMap.get(message.getMetaIdx() + "_exit");
//                // 7. 5에서 0.3초 대기한 후에도 6에서 가져온 DTO가 여전히 존재하는지 체크한다.
//                // 7-1. 퇴장 메시지가 존재하는 경우 - 퇴장
//                if ( exitMessage == null ) {
//                    // 7-1-1. 퇴장한 것이 아니기에 더 이상 작업할 것이 없다.
//                // 7-2. 퇴장 메시지가 존재하지 않는 경우 - 재입장(새로고침)
//                } else {
//                    // 8. 4에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 삭제한다.
//                    metaMessageMap.remove(message.getMetaIdx() + "_exit");
//                    // 9. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 Map을 가져온다.
//                    Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(message.getMetaIdx());
//                    // 10. 9에서 가져온 Map에서, 1에서 파라미터로 받아온 DTO 값 중 작성자(퇴장자) 키에 해당하는 List를 제거한다.
//                    metaCanvasMap.remove(message.getWriter());
//                    // 11. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 9에서 가져온 Map을 JSON 문자열로 변환한다.
//                    String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
//                    // 13. 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임이 존재하는지 체크한다.
//                    // 13-1. 방장 닉네임이 존재하지 않는 경우
//                    if ( message.getMaster() == null ) {
//                        // 13-1-1. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                        //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                        //         "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
//                        template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
//                        // 13-1-2. 11에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
//                        message.setExit(metaCanvasJson);
//                        // 13-1-3. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                        //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                        //         "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
//                        template.convertAndSend("/sub/meta/caferoom/canvas/" + message.getMetaIdx(), message);
//                    // 13-2. 방장 닉네임이 존재하는 경우
//                    } else {
//                        // 14. 참여중인 인원이 0명인지 체크한다.
//                        // 14-1. 참여중인 인원이 0명인 경우 - 방 삭제
//                        if ( message.getMetaRecruitingPersonnel() == 0 ) {
//                            // 14-1-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 가지고 metaController에 방 삭제 메소드를 호출하여 방을 퇴장하고 삭제한다.
//                            metaController.deleteRoomMaster(message.getMetaIdx());
//                        // 14-2. 참여중인 인원이 0명이 아닌 경우 - 방 유지
//                        } else {
//                            // 14-2-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 키로 사용하고, 1에서 파라미터로 받아온 DTO 값 중 방장 닉네임을 값으로 사용하여, 위에서 생성한 방장 재입장 체크용 Map에 추가한다.
//                            boomMetaRoom.put(message.getMetaIdx(), message.getMaster());
//                            // 14-2-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                            //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                            //         "/sub" + "/meta/caferoom/" + metaIdx = "/sub/meta/caferoom/1"
//                            template.convertAndSend("/sub/meta/caferoom/" + message.getMetaIdx(), message);
//                            // 13-2-3. 11에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 퇴장 후 캐릭터 Map에 setter를 통해 전달한다.
//                            message.setExit(metaCanvasJson);
//                            // 14-2-4. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
//                            //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
//                            //         "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
//                            template.convertAndSend("/sub/meta/caferoom/canvas/" + message.getMetaIdx(), message);
//                            // 14-2-5. 퇴장 메시지를 전송하고 난 후 1분 대기하여 방장이 완전한 퇴장인지 다시 재입장 하는지 체크한다.
//                            Thread.sleep(60000);
//                            // 14-2-6. 14-2-1에서 방장 재입장 체크용 Map에 추가한 키에 해당하는 값을 삭제한다.
//                            boomMetaRoom.remove(message.getMetaIdx());
//                        }
//                    }
//                }
//            } catch (InterruptedException e) { // 스레드를 중지하거나 중단시킬 때 발생할 수 있는 예외가 발생한 경우 catch문이 실행된다.
//                // InterruptedException - 스레드가 sleep() 상태에서 interrupt() 메소드 호출로 깨어나서 예외를 발생시킨 경우 발생한다.
//                //                        스레드가 인터럽트되면 스레드가 일시 중단되고, 인터럽트 상태 플래그가 true로 설정되는데,
//                //                        이 경우, Thread.currentThread().interrupt() 메소드를 호출하여 인터럽트 상태 플래그를 재설정해준다.
//                // Thread.currentThread().interrupt() - 스레드를 강제 종료시키는 것이 아니라, 인터럽트 플래그를 설정함으로써 스레드가 계속 실행되지 않도록 만드는 메소드이다.
//                Thread.currentThread().interrupt(); // 현재 스레드에 인터럽트 플래그를 설정하여, 스레드가 계속 실행되지 않도록 만든다.
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
    ////////////////////////////////////////////////// 카페룸 캔버스 구역 //////////////////////////////////////////////////
    // 카페룸 캔버스 첫 입장
    @MessageMapping(value = "/meta/caferoom/canvas/enter")
    public void canvasEnterCafeRoom(MetaCanvasDTO canvas) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 캐릭터 정보들을 DTO로 받아온다.
        // 2. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 캐릭터 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(canvas.getMetaIdx());
        // 3. 2에서 가져온 캐릭터 Map이 존재하는지 체크한다.
        // 3-1. Map이 존재하지 않는 경우
        if ( metaCanvasMap == null ) {
            // 3-1-1. 2에서 가져오는 캐릭터 Map을 생성한다.
            metaCanvasMap = new HashMap<>();
            // 3-1-2. 3-1-1에서 생성한 캐릭터 Map에 값으로 사용할 캐릭터 List를 생성한다.
            List<Object> metaCoordinateList = new ArrayList();
            // 3-1-3. 3-1-2에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 캐릭터 이름을 전달한다.
            metaCoordinateList.add(canvas.getCharacter());
            // 3-1-4. 3-1-2에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 x축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getX());
            // 3-1-5. 3-1-2에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 y축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getY());
            // 3-1-6. 1에서 파라미터로 받아온 DTO 값 중 닉네임을 키로 사용하고, 3-1-2에서 생성한 캐릭터 List를 값으로 사용하여, 3-1-1에서 생성한 캐릭터 Map에 추가한다.
            metaCanvasMap.put(canvas.getWriter(), metaCoordinateList);
            // 3-1-7. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 키로 사용하고, 3-1-6에서 캐릭터 List가 추가된 캐릭터 Map을 값으로 사용하여, 위에서 생성한 방 구분용 Map에 추가한다.
            metaRoomMap.put(canvas.getMetaIdx(), metaCanvasMap);
            // 3-1-8. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 3-1-1에서 생성한 캐릭터 Map을 JSON 문자열로 변환한다.
            String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
            // 3-1-9. 3-1-8에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
            canvas.setCharacters(metaCanvasJson);
            // 3-1-10. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
            //         path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
            //         "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
            template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas);
        // 3-2. Map이 존재하는 경우
        } else {
            // 3-2-1. 2에서 가져온 캐릭터 Map에 값으로 사용할 캐릭터 List를 생성한다.
            List<Object> metaCoordinateList = new ArrayList();
            // 3-2-2. 3-2-1에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 캐릭터 이름을 전달한다.
            metaCoordinateList.add(canvas.getCharacter());
            // 3-2-3. 3-2-1에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 x축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getX());
            // 3-2-4. 3-2-1에서 생성한 캐릭터 List에 1에서 파라미터로 받아온 DTO 값 중 y축 좌표를 전달한다.
            metaCoordinateList.add(canvas.getY());
            // 3-2-5. 1에서 파라미터로 받아온 DTO 값 중 닉네임을 키로 사용하고, 3-2-1에서 생성한 캐릭터 List를 값으로 사용하여, 2에서 가져온 캐릭터 Map에 추가한다.
            metaCanvasMap.put(canvas.getWriter(), metaCoordinateList);
            // 3-2-6. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 2에서 가져온 캐릭터 Map을 JSON 문자열로 변환한다.
            String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
            // 3-2-7. 3-2-6에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
            canvas.setCharacters(metaCanvasJson);
            // 3-2-8. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
            //        "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
            template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas);
        }
    }

    // 카페룸 캔버스 첫 입장 이후 재입장 - 첫 입장 이후 모든 재입장은 이곳으로 들어온다.
    @MessageMapping(value = "/meta/caferoom/canvas/reenter")
    public void canvasReEnterCafeRoom(MetaCanvasDTO canvas) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 캐릭터 정보들을 DTO로 받아온다.
        // 2. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 캐릭터 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(canvas.getMetaIdx());
        // 3. 2에서 가져온 캐릭터 Map이 존재하는지 체크한다.
        // 3-1. Map이 존재하지 않는 경우 - 재입장 에러
        if ( metaCanvasMap == null ) {
            // 3-1-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "reErr"로 변경한다. - 재입장 에러 값
            canvas.setType("reErr");
            // 3-1-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
            //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
            //        "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
            template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas);
        // 3-2. Map이 존재하는 경우 - 재입장(새로고침)
        } else {
            // 4. 2에서 가져온 캐릭터 Map에서, 1에서 파라미터로 받아온 DTO 값 중 닉네임 키에 해당하는 캐릭터 List를 가져온다.
            List<Object> metaCoordinateList = metaCanvasMap.get(canvas.getWriter());
            // 5. 4에서 가져온 캐릭터 List가 존재하는지 체크한다.
            // 5-1. List가 존재하지 않는 경우 - 재입장 에러
            if (metaCoordinateList == null) {
                // 5-1-1. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 setter를 통해 "reErr"로 변경한다. - 재입장 에러 값
                canvas.setType("reErr");
                // 5-1-2. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
                template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas);
            // 5-2. List가 존재하는 경우 - 재입장(새로고침)
            } else {
                // 5-2-1. 4에서 가져온 캐릭터 List 값 중 x좌표 값을, 1에서 파라미터로 받아온 DTO 값 중 x좌표 값으로 갱신한다.
                metaCoordinateList.set(1, canvas.getX());
                // 5-2-2. 4에서 가져온 캐릭터 List 값 중 y좌표 값을, 1에서 파라미터로 받아온 DTO 값 중 y좌표 값으로 갱신한다.
                metaCoordinateList.set(2, canvas.getY());
                // 5-2-3. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 2에서 가져온 캐릭터 Map을 JSON 문자열로 변환한다.
                String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
                // 5-2-4. 5-2-3에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
                canvas.setCharacters(metaCanvasJson);
                // 5-2-5. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 1에서 파라미터로 받아온 DTO를 다시 전달한다.
                //        path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 1에서 파라미터로 받아온 DTO 값 중 방 번호가 병합된다.
                //        "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
                template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas);
            }
        }
    }

    // 카페룸 캔버스 캐릭터 이동 좌표 변경 - RabbitMQ 사용
    @MessageMapping(value = "/meta/caferoom/canvas/move")
    public void canvasMoveCafeRoom(MetaCanvasDTO canvas) throws JsonProcessingException { // 1. 클라이언트로부터 전송된 캐릭터 정보들을 DTO로 받아온다.
        // 2. 위에서 생성한 방 구분용 Map에서, 1에서 파라미터로 받아온 DTO 값 중 방 번호 키에 해당하는 캐릭터 Map을 가져온다.
        Map<String, List<Object>> metaCanvasMap = metaRoomMap.get(canvas.getMetaIdx());
        // 3. 2에서 가져온 캐릭터 Map에서, 1에서 파라미터로 받아온 DTO 값 중 닉네임 키에 해당하는 캐릭터 List를 가져온다.
        List<Object> metaCoordinateList = metaCanvasMap.get(canvas.getWriter());
        // 4. 1에서 파라미터로 받아온 DTO 값 중 메시지 타입을 가져와 이동 분기를 결정한다.
        switch( canvas.getType() ) {
            // 4-1. 왼쪽으로 이동
            case "left":
                // 왼쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                // 왼쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-1-1. 3에서 가져온 캐릭터 List 값 중 x좌표 값을, 5 뺀 값으로 갱신한다.
                    metaCoordinateList.set(1, (int) metaCoordinateList.get(1) - 5);
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                }
            // 4-2. 위로 이동
            case "top":
                // 위쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                // 위쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-2-1. 3에서 가져온 캐릭터 List 값 중 y좌표 값을, 5 뺀 값으로 갱신한다.
                    metaCoordinateList.set(2, (int) metaCoordinateList.get(2) - 5);
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                }
            // 4-3. 오른쪽으로 이동
            case "right":
                // 오른쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                // 오른쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-3-1. 3에서 가져온 캐릭터 List 값 중 x좌표 값을, 5 더한 값으로 갱신한다.
                    metaCoordinateList.set(1, (int) metaCoordinateList.get(1) + 5);
                    // 위쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) < canvas.getCanvasTop() ) {
                        break;
                    }
                    // 아래쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                        break;
                    }
                    break;
                }
            // 4-4. 아래로 이동
            case "bottom":
                // 아래쪽 벽이 나오면 멈춘다.
                if ( (int) metaCoordinateList.get(2) > canvas.getCanvasBottom() ) {
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                // 아래쪽 벽이 나오기 전까지 움직인다.
                } else {
                    // 4-4-1. 3에서 가져온 캐릭터 List 값 중 y좌표 값을, 5 더한 값으로 갱신한다.
                    metaCoordinateList.set(2, (int) metaCoordinateList.get(2) + 5);
                    // 왼쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) < canvas.getCanvasLeft() ) {
                        break;
                    }
                    // 오른쪽 벽이 나오면 멈춘다.
                    if ( (int) metaCoordinateList.get(1) > canvas.getCanvasRight() ) {
                        break;
                    }
                    break;
                }
        }
        // 5. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 2에서 가져온 캐릭터 Map을 JSON 문자열로 변환한다.
        String metaCanvasJson = objectMapper.writeValueAsString(metaCanvasMap);
        // 6. 5에서 변환한 JSON 문자열을 1에서 파라미터로 받아온 DTO 값 중 캐릭터 정보에 setter를 통해 전달한다.
        canvas.setCharacters(metaCanvasJson);

//        // 7번과 8번은 RabbitMQConfig에서 미리 메시지 변환기를 만들어 설정해놨기에 사용할 필요가 없다.
//        // 7. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 1에서 파라미터로 받아온 DTO를 JSON 문자열로 변환한다.
//        String json = objectMapper.writeValueAsString(canvas);
//        // 8. Spring AMQP의 MessageBuilder 클래스를 사용하여 AMQP 메시지를 생성한다.
//        //    MessageBuilder.withBody(json.getBytes()) - 7에서 변환한 JSON 문자열을 byte 배열로 변환하여 AMQP 메시지의 body에 전달한다.
//        //    .setContentType("application/json") - 생성된 AMQP 메시지의 contentType을 "application/json"으로 설정한다.
//        //                                        - 이는 메시지의 body가 어떤 형식인지를 나타내는 값이다.
//        //    .build() - 설정이 완료된 AMQP 메시지를 빌드하여 반환한다.
//        Message message = MessageBuilder.withBody(json.getBytes())
//                                        .setContentType("application/json")
//                                        .build();

        // 9. 위에서 @Autowired로 생성한 RabbitTemplate을 사용하여 8에서 생성한 메시지를 전송한다.
        //    첫 번째 인자는 메시지를 발송할 Exchange 이름으로, 메시지를 받아서 어느 큐로 보낼지 결정하는 역할을 한다.
        //    두 번째 인자는 메시지를 전송할 Routing Key 이름으로, Exchange에서 메시지를 받아서 어느 큐로 보낼지 결정하는 규칙을 나타내는 값이다.
        //    세 번째 인자는 전송할 메시지 객체이다.
        //    convertAndSend() - 인자로 받은 메시지를 RabbitMQ Broker에 전송한다.
        rabbitTemplate.convertAndSend("CafeRoomExchange", "CafeRoomRoutingKey", canvas);
//        template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas);
    }

    // 카페룸 캔버스 캐릭터 이동 좌표 전달 - RabbitMQ 사용
    // @RabbitListener(queues = "CafeRoomQueue") - RabbitMQ 메시지 큐의 "CafeRoomQueue"라는 큐를 대상으로 메시지를 수신하는 리스너 함수임을 선언한다.
    // ackMode = "MANUAL" - ackMode는 수신한 메시지의 처리가 완료되면 RabbitMQ에 알려주는 방식을 설정하는 것으로,
    //                      "MANUAL"로 설정하면  메시지를 수신한 후 명시적으로 channel.basicAck()를 호출하여 메시지를 처리 완료했다는 신호를 보내야 한다.
    @RabbitListener(queues = "CafeRoomQueue", ackMode = "MANUAL")
    public void receiveMessageCafeRoom(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) { // 1. RabbitMQ로부터 수신된 메시지를 파라미터로 받아온다.
                                                                                                                       //    Channel - 메시지 큐와 통신할 수 있는 채널이다.
                                                                                                                       //    @Header(AmqpHeaders.DELIVERY_TAG) - 메시지를 수신할 때 해당 메시지의 "delivery tag" 값을 가져오는 역할을 한다.
                                                                                                                       //                                        "delivery tag"는 RabbitMQ가 메시지의 유일성을 보장하기 위해 각 메시지에 할당한 일련번호이다.
        try {
//            // 2번은 RabbitMQConfig에서 미리 메시지 변환기를 만들어 설정해놨기에 사용할 필요가 없다.
//            // 2. 1에서 파라미터로 받아온 메시지를 UTF-8로 인코딩하여 String 형태로 변환한다.
//            //    message.getBody() - RabbitMQ로부터 수신된 메시지의 body를 바이트 배열 형태로 반환한다.
//            //    new String(byte[] bytes, String charset) - 생성자를 사용하여 바이트 배열을 문자열로 변환하며,
//            //                                               이 때 charset 인자에는 인코딩 방식(UTF-8)을 지정해주어야 한다.
//            String json = new String(message.getBody(), "UTF-8");

            // 3. 위에서 @Autowired로 생성한 ObjectMapper를 사용하여 1에서 파라미터로 받아온 JSON 형식의 메시지를 DTO로 변환한다.
            //    readValue - JSON 문자열을 Java 객체로 역직렬화하는 메소드로,
            //                첫 번째 인자로 역직렬화할 대상인 JSON 문자열을,
            //                두 번째 인자로 역직렬화할 타입인 Java 객체의 클래스를 전달받는다.
            MetaCanvasDTO canvas = objectMapper.readValue(message.getBody(), MetaCanvasDTO.class);
            // 4. 메시지가 잘 처리됬는지 체크한다.
            // 4-1. 메시지가 잘 처리됬을 경우
            //      RabbitMQ는 완료한 메시지를 삭제하고, 큐에서 제거한다.
            //      basicAck - RabbitMQ에 메시지 처리 완료를 알리는 메소드이다.
            channel.basicAck(tag, false);
            // 5. SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 3에서 변환한 DTO를 다시 전달한다.
            //    path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 3에서 변환한 DTO 값 중 방 번호가 병합된다.
            //    "/sub" + "/meta/caferoom/canvas/" + metaIdx = "/sub/meta/caferoom/canvas/1"
            template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas);

//            // 메시지 헤더 설정 - 헤더에 추가할 것이 있을 경우 사용한다.
//            SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
//            headers.setLeaveMutable(true);
//            headers.setNativeHeader("clear-cache", "true");
//            template.convertAndSend("/sub/meta/caferoom/canvas/" + canvas.getMetaIdx(), canvas, headers.getMessageHeaders());
        } catch (Exception e) {
            try {
                // 4-2. 메시지가 잘 처리되지 않았을 경우
                //      RabbitMQ는 실패한 메시지를 다시 큐로 보내고, 3번째 파라미터인 requeue를 true로 설정하여 재처리한다.
                //      basicNack - RabbitMQ에 메시지 처리 실패를 알리는 메소드이다.
                channel.basicNack(tag, false, true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
/////////////////////////////////////////////////////// 자습실 구역 ///////////////////////////////////////////////////////
//    // 자습실 첫 입장 - 폐기
//    @MessageMapping(value = "/meta/oneroom/enter")
//    public void enterOneRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 첫 입장 정보들을 DTO로 받아온다.
//
//    }

//    // 자습실 첫 입장 이후 재입장 - 첫 입장 이후 모든 재입장은 이곳으로 들어온다. - 폐기
//    @MessageMapping(value = "/meta/oneroom/reenter")
//    public void reEnterOneRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 재입장(새로고침) 정보들을 DTO로 받아온다.
//        // 2. 이전 퇴장 메소드에서 Map에 추가한 키에 해당하는 DTO를 다시 가져온다.
//        ChatMessageDTO exitMessage = metaMessageMap.get(message.getMetaIdx() + "_exit");
//        // 3. 2에서 가져온 DTO가 여전히 존재하는지 체크한다.
//        // 3-1. 퇴장 메시지가 존재하는 경우 - 재입장(새로고침)
//        if ( exitMessage != null ) {
//            // 3-1-1. 퇴장 메소드에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 삭제한다.
//            metaMessageMap.remove(message.getMetaIdx() + "_exit");
//        // 3-2. 퇴장 메시지가 존재하지 않는 경우 - 0.3초가 넘는 장시간의 새로고침 에러로 인한 방 퇴장 및 삭제 처리 후 재입장된 것으로,
//        //                                 이는 아직 퇴장한 것이 아닌데 에러로 인해 방 퇴장 및 삭제 처리가 되었으므로 다시 새로운 방을 생성해 입장 처리를 해준다.
//        } else {
////            // 3-2-1. 방 생성에 필요한 방 생성 DTO를 생성한다.
////            Meta.rqCreateMeta rqCreateMeta = new Meta.rqCreateMeta();
////            // 3-2-2. 3에서 저장하고 받아온 Entity 값 중 방 번호를 setter를 통하여 방 생성 DTO 값 중 방 타입에 전달한다.
////            rqCreateMeta.setMetaType("oneRoom");
////            // 3-2-3. 1에서 파라미터로 받아온 DTO 값 중 작성자를 setter를 통하여 방 생성 DTO 값 중 방장에 전달한다.
////            rqCreateMeta.setMetaMaster(message.getWriter());
////            // 3-2-4. 위에서 값들이 전달된 방 생성 DTO를 가지고 metaController에 방 생성 메소드를 호출하여 새로운 방을 생성하고 입장한다.
////            metaController.reCreateMetaRoom(rqCreateMeta);
//        }
//    }

    // 자습실 퇴장
    @MessageMapping(value = "/meta/oneroom/exit")
    public void exitOneRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 퇴장 정보들을 DTO로 받아온다.
        // 2. metaController에서 재입장(새로고침) 체크용 Map에 추가한 작성자(퇴장자) 키에 해당하는 재입장(새로고침) 체크 값을 가져온다.
        Long reEnterCheck = metaController.reEnterCheck.get(message.getWriter());
        // 3. 2에서 가져온 재입장(새로고침) 체크 값이 존재하는지 체크한다.
        // 3-1. 재입장(새로고침) 체크 값이 존재하는 경우 - 재입장(새로고침)
        if ( reEnterCheck != null ) {
            // 3-1-1. metaController에서 재입장(새로고침) 체크용 Map에 추가한 작성자(퇴장자) 키에 해당하는 재입장(새로고침) 체크 값을 제거한다.
            metaController.reEnterCheck.remove(message.getWriter());
            // 3-1-2. 퇴장한 것이 아니기에 더 이상 작업할 것이 없다.
        // 3-2. 재입장(새로고침) 체크 값이 존재하지 않는 경우 - 퇴장
        } else {
            // 3-2-1. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 가지고 metaController에 방 삭제 메소드를 호출하여 방을 퇴장하고 삭제한다.
            metaController.deleteRoomMaster(message.getMetaIdx());
        }
    }

//    // 자습실 퇴장 - 폐기
//    @MessageMapping(value = "/meta/oneroom/exit")
//    // Future - Future 인터페이스는 Java5부터 java.util.concurrency 패키지에서 비동기의 결과값을 받는 용도로 사용했지만 비동기의 결과값을 조합하거나, error를 핸들링할 수가 없었다.
//    // CompletionStage - Java 8에서 추가된 인터페이스 중 하나로, 비동기식 계산 결과를 다루기 위한 일종의 통합 API이다.
//    //                   CompletableFuture 클래스와 함께 사용되어, 비동기 작업을 수행하고 작업 결과를 처리하는 기능을 제공한다.
//    // CompletableFuture - Java 8에서 추가된 클래스 중 하나로, 비동기 작업을 수행하고 해당 작업의 결과를 처리하는 기능을 제공한다.
//    // <Void> - runAsync는 반환 값이 없으므로 Void 타입이다.
//    public CompletableFuture<Void> exitOneRoom(ChatMessageDTO message) { // 1. 클라이언트로부터 전송된 퇴장 정보들을 DTO로 받아온다.
//        // 2. 1에서 파라미터로 받아온 DTO 값 중 참여중인 인원을 가져와 1 감소시킨 뒤 다시 참여중인 인원에 setter를 통해 전달한다.
//        message.setMetaRecruitingPersonnel(message.getMetaRecruitingPersonnel() - 1);
//
//        // 3. 1에서 파라미터로 받아온 DTO 값 중 방 번호와 퇴장을 의미하는 문자를 조합하여 키로 사용하고, 1에서 파라미터로 받아온 DTO를 값으로 사용하여, 위에서 생성한 새로고침 체크용 Map에 추가한다.
//        metaMessageMap.put(message.getMetaIdx() + "_exit", message);
//
//        // CompletableFuture.runAsync - 비동기적으로 실행되는 작업을 수행하는 CompletableFuture 객체를 반환한다.
//        // runAsync() - 파라미터로 Runnable 객체를 받으며, 이 객체의 run() 메소드 안에 비동기적으로 실행할 작업을 구현한다.
//        //              메소드는 즉시 리턴하며, 별도의 쓰레드에서 run() 메소드 안에 구현된 작업이 비동기적으로 실행된다.
//        // Runnable - 인자를 받지 않고, 리턴값도 없는 함수형 인터페이스이다.
//        //            run() 메소드를 하나만 가지고 있으며, 이 메소드에서 수행될 작업을 구현한다.
//        //            run() 메소드는 매개변수를 받지 않으며, 리턴값도 없다.
//        return CompletableFuture.runAsync(() -> {
//            try {
//                // 4. 퇴장 메시지를 전송하기 전에 0.3초 대기하여 퇴장인지 재입장(새로고침)인지 체크한다.
//                Thread.sleep(300);
//                // 5. 3에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 다시 가져온다.
//                ChatMessageDTO exitMessage = metaMessageMap.get(message.getMetaIdx() + "_exit");
//                // 6. 4에서 0.3초 대기한 후에도 5에서 가져온 DTO가 여전히 존재하는지 체크한다.
//                // 6-1. 퇴장 메시지가 존재하는 경우 - 퇴장
//                if ( exitMessage == null ) {
//                    // 6-1-1. 퇴장한 것이 아니기에 더 이상 작업할 것이 없다.
//                // 6-2. 퇴장 메시지가 존재하지 않는 경우 - 재입장(새로고침)
//                } else {
//                    // 6-2-1. 4에서 새로고침 체크용 Map에 추가한 키에 해당하는 DTO를 삭제한다.
//                    metaMessageMap.remove(message.getMetaIdx() + "_exit");
//                    // 6-2-2. 1에서 파라미터로 받아온 DTO 값 중 방 번호를 가지고 metaController에 방 삭제 메소드를 호출하여 방을 퇴장하고 삭제한다.
//                    metaController.deleteRoomMaster(message.getMetaIdx());
//                }
//            } catch (InterruptedException e) { // 스레드를 중지하거나 중단시킬 때 발생할 수 있는 예외가 발생한 경우 catch문이 실행된다.
//                // InterruptedException - 스레드가 sleep() 상태에서 interrupt() 메소드 호출로 깨어나서 예외를 발생시킨 경우 발생한다.
//                //                        스레드가 인터럽트되면 스레드가 일시 중단되고, 인터럽트 상태 플래그가 true로 설정되는데,
//                //                        이 경우, Thread.currentThread().interrupt() 메소드를 호출하여 인터럽트 상태 플래그를 재설정해준다.
//                // Thread.currentThread().interrupt() - 스레드를 강제 종료시키는 것이 아니라, 인터럽트 플래그를 설정함으로써 스레드가 계속 실행되지 않도록 만드는 메소드이다.
//                Thread.currentThread().interrupt(); // 현재 스레드에 인터럽트 플래그를 설정하여, 스레드가 계속 실행되지 않도록 만든다.
//            }
//        });
//    }
}
