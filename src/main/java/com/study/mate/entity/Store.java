package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
@Entity(name="Store") // Entity 어노테이션 - 괄호안에는 테이블명과 똑같이 작성한다.
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeIdx;

    @Column(length = 50, nullable = false)
    private String goods;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(nullable = false)
    private int price;

    @Column(length = 255, nullable = false)
    private String introduce;

    @Column(nullable = false)
    private int stock;

    private int goodsLike;

    @Column(length = 50, nullable = false)
    private String itemName;

    @Column(nullable = false,length = 50)
    private String image;
}
