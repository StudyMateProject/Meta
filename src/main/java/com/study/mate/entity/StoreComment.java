package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
@Entity(name="StoreComment")
public class StoreComment {

    @Id // 기본키 어노테이션 - 기본키 설정 (PRIMARY KEY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private Long commentIdx;

    @Column(nullable = false, length = 20)
    private String writer;

    @Column(nullable = false, length = 100)
    private String comment;

    @Column(nullable = false)
    private int deleteCheck;

}
