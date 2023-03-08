package com.study.soju.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Autowired
    private ConnectionFactory connectionFactory;

    // Jackson2JsonMessageConverter를 사용하여 JSON 메시지 변환기를 생성합니다.
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate을 생성하고 메시지 변환기를 설정합니다.
    // 전송 중에 예외가 발생하면 메시지가 반환되도록 설정합니다.
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    // "MsgQueue"라는 이름의 큐를 생성합니다.
    // TTL이나 DLX 설정을 추가할 수 있습니다.
//    @Bean
//    public Queue queue() {
//        return QueueBuilder.durable("MsgQueue")
//                //.withArgument("x-dead-letter-exchange", "dlxExchange")
//                //.withArgument("x-message-ttl", 5000)
//                .build();
//    }
    @Bean
    public Queue queue() {
        return new Queue("MsgQueue");
    }

    // "MsgExchange"라는 이름의 다이렉트 교환기를 생성합니다.
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("MsgExchange");
    }

    // "MsgRoutingKey"라는 라우팅 키를 사용하여 큐와 교환기를 바인딩합니다.
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("MsgRoutingKey");
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    // DLX용 교환기와 바인딩 된 DLQ를 생성합니다.
    @Bean
    public FanoutExchange dlxExchange() {
        return new FanoutExchange("dlxExchange");
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable("dlq")
                .build();
    }

    @Bean
    public Binding dlxBinding(Queue dlq, FanoutExchange dlxExchange) {
        return BindingBuilder.bind(dlq).to(dlxExchange);
    }
}
