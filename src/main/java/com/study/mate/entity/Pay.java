package com.study.mate.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
@Entity(name="Pay") // Entity 어노테이션 - 괄호안에는 테이블명과 똑같이 작성한다.
public class Pay {
    @Id
    @Column(length = 200)
    private String impUid;

    @Column(length = 200, nullable = false)
    private String merchantUid;

    @Column(length = 50, nullable = false)
    private String PGName;

    @Column(length = 300)
    private String payMethod;

    @Column(length = 50, nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int price;

    @Column(length = 50, nullable = false)
    private String buyerEmail;

    @Column(length = 20, nullable = false)
    private String buyerName;

    @Column(length = 20, nullable = false)
    private String buyerTel;

    @Column(length = 50)
    private String buyerAddress;

    @Column(length = 50)
    private String buyerPostNum;

    @Column(nullable = false)
    private int itemCount;

    @Column(nullable = false)
    private int isPaid;
    //0 : 결제 완료
    //1 : 환불 처리중
    //2 : 환불 완료
}
