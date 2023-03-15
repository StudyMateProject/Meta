package com.study.soju.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@EnableWebSocketMessageBroker // Stomp를 사용하기위해 선언하는 어노테이션
@Configuration
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //* endpoint 를 "/ws/meta" 로 설정하였는데 만약 해당 프로젝트에 스프링 시큐리티를 적용했다면 시큐리티에서 "/ws/meta/**" 경로는 통신을 허용하도록 해줘야 크로스도메인 관련 에러메시지가 발생하지 않는다.
    //
    //* setAllowOrigins() 설정도 역시 크로스도메인 이슈로 인해 필요한 설정이다.
    // 웹소켓은 연결할 때의 도메인주소와 요청할 때의 도메인주소가 일치해야 통신이 된다.
    // 만약 http://localhost:8080/ws/meta 로 웹소켓 객체를 생성했는데(설정시점) http://localhost 로 웹소켓 통신을 시도하면(요청시점) connect가 이뤄지지 않는다.
    // 정확히 포트번호까지 일치해야 한다.
    // 따라서 설정된 도메인 외에 연결을 허용할 도메인을 지정해주면 해당 문제를 피할 수 있다.

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/meta") // Client가 WebSocket 또는 SockJS에 connect할 경로
                .setAllowedOrigins("http://localhost:8888") // 도메인주소
                .withSockJS(); // SockJS
    }

    // 여기서는 송수신에 따라 각 접두사로 path를 지정하고, StompChatController 및 JavaScript에서 나머지 path를 지정할 수 있다.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // setApplicationDestinationPrefixes : Client에서 전송한 SEND 요청을 처리
        registry.setApplicationDestinationPrefixes("/pub"); // 송신 - 전송 방향 : 클라이언트 --> 서버
        // enableSimpleBroker : 해당 경로로 SimpleBroker를 등록, SimpleBroker는 해당 경로를 SUBSCRIBE하는 Client에게 메세지를 전달하는 간단한 작업을 수행
        registry.enableSimpleBroker("/sub") // 수신 - 전송 방향 : 서버 --> 클라이언트
                .setHeartbeatValue(new long[]{10000, 10000}) // heartbeat 설정
                .setTaskScheduler(heartBeatScheduler());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(1024 * 1024); // 1MB로 설정
    }

    @Bean
    public TaskScheduler heartBeatScheduler() {
        return new ConcurrentTaskScheduler();
    }
}
