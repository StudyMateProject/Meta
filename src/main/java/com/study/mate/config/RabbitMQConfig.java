package com.study.mate.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Spring에서 RabbitMQ와 연결을 담당하는 인터페이스이다.
    @Autowired
    private ConnectionFactory connectionFactory;

    // Jackson2JsonMessageConverter를 사용하여 메시지 변환기를 생성한다.
    // Jackson2JsonMessageConverter - JSON 형식의 메시지를 생성하고 수신할 수 있게 도와주는 Spring의 메시지 변환기이다.
    // RabbitTemplate에서 Jackson2JsonMessageConverter를 사용하여,
    // RabbitMQ로 메시지를 보낼 때는 메시지를 직렬화하여 JSON 형식으로 변환하여 보내며,
    // RabbitMQ로부터 메시지를 받을 때는 JSON 형식으로 받은 메시지를 JAVA 객체로 역직렬화하여 사용한다.
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate 객체를 생성하고, 해당 객체를 사용하여 RabbitMQ와의 통신을 수행할 때 JSON 형식의 메시지를 보내도록 설정한다.
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        // RabbitTemplate 객체는 위에서 @Autowired로 생성한 ConnectionFactory를 생성자 인자로 받아서 생성한다.
        // RabbitTemplate - RabbitMQ와의 통신을 담당하는 객체이다.
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 메시지 변환기(MessageConverter)를 위에서 생성한 jsonMessageConverter() 메소드로 설정한다.
        // 이 설정은 해당 RabbitTemplate으로 메시지를 보낼 때 메시지 내용을 JSON 형식으로 변환해준다.
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // 필수 속성(Mandatory)을 설정한다.
        // 필수 속성을 설정하면 메시지 라우팅이 실패한 경우, 해당 메시지를 반드시 반환하도록 강제한다.
        rabbitTemplate.setMandatory(true);
        // 설정이 끝난 rabbitTemplate을 반환한다.
        return rabbitTemplate;
    }

    // "MsgQueue"라는 이름을 가진 큐를 생성한다.
    // TTL이나 DLX 설정을 추가할 수 있다.
//    @Bean
//    public Queue queue() {
//        return QueueBuilder.durable("MsgQueue")
//                //.withArgument("x-dead-letter-exchange", "dlxExchange")
//                //.withArgument("x-message-ttl", 5000)
//                .build();
//    }

    // "MsgQueue"라는 이름을 가진 큐를 생성한다.
    @Bean
    public Queue studyRoomQueue() {
        // Queue를 생성한다.
        // 해당 Queue를 사용하여 RabbitMQ에 메시지를 보낼 수 있다.
        return new Queue("StudyRoomQueue");
    }
    @Bean
    public Queue cafeRoomQueue() {
        // Queue를 생성한다.
        // 해당 Queue를 사용하여 RabbitMQ에 메시지를 보낼 수 있다.
        return new Queue("CafeRoomQueue");
    }

    // "MsgExchange"라는 이름을 가진 Direct Exchange를 생성한다.
    @Bean
    public DirectExchange studyRoomExchange() {
        // Direct Exchange를 생성한다.
        // Direct Exchange는 Exchange에 바인딩된 Queue들 중에 Routing Key와 일치하는 Queue로만 메시지를 전송한다.
        return new DirectExchange("StudyRoomExchange");
    }
    @Bean
    public DirectExchange cafeRoomExchange() {
        // Direct Exchange를 생성한다.
        // Direct Exchange는 Exchange에 바인딩된 Queue들 중에 Routing Key와 일치하는 Queue로만 메시지를 전송한다.
        return new DirectExchange("CafeRoomExchange");
    }

    // Queue("RoomQueue")와 Exchange("RoomExchange")를 Routing Key("RoomRoutingKey")로 Binding해주는 역할을 한다.
    // 이렇게 설정된 Binding은 Producer가 Exchange("RoomExchange")로 Routing Key("RoomRoutingKey")를 가진 메시지를 전송하면,
    // 해당 메시지는 Queue("RoomQueue")로 전송된다.
    @Bean
    public Binding studyRoomBinding(Queue studyRoomQueue, DirectExchange studyRoomExchange) { // Queue 객체와 DirectExchange 객체를 파라미터로 받는다.
        // queue 객체를 exchange 객체와 "StudyRoomRoutingKey"로 바인딩한다.
        return BindingBuilder.bind(studyRoomQueue).to(studyRoomExchange).with("StudyRoomRoutingKey");
    }
    @Bean
    public Binding cafeRoomBinding(Queue cafeRoomQueue, DirectExchange cafeRoomExchange) { // Queue 객체와 DirectExchange 객체를 파라미터로 받는다.
        // queue 객체를 exchange 객체와 "CafeRoomRoutingKey"로 바인딩한다.
        return BindingBuilder.bind(cafeRoomQueue).to(cafeRoomExchange).with("CafeRoomRoutingKey");
    }

    // RabbitMQ의 관리 기능을 활용하기 위한 RabbitAdmin 객체를 생성한다.
    @Bean
    public RabbitAdmin rabbitAdmin() {
        // RabbitAdmin 객체는 위에서 @Autowired로 생성한 ConnectionFactory를 생성자 인자로 받아서 생성한다.
        // RabbitAdmin - RabbitMQ 서버에게 관리 작업을 수행하기 위한 API를 제공하는 클래스이다.
        // 큐를 생성하거나 삭제하거나, 메시지를 조회하거나, 교환기를 생성하거나, 그리고 교환기와 큐 사이의 바인딩을 설정하거나 해제하는 등의 작업을 수행할 수 있다.
        return new RabbitAdmin(connectionFactory);
    }
}
