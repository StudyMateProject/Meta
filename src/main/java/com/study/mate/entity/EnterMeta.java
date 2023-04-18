package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
@Entity(name = "EnterMeta") // Entity 어노테이션 - 괄호안에는 테이블명과 똑같이 작성한다.
public class EnterMeta {
    @Id // 기본키 어노테이션 - 기본키 설정 (PRIMARY KEY)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT - MySQL에서 시퀀스 역할을 담당한다.
    private Long idx; // MySQL에서 AUTO_INCREMENT를 사용하면 null값이 들어가야 자동으로 숫자가 올라간다.
                      // 하지만 long 즉, 원시타입으로 작성하면 null값을 허용하지 않기 때문에 오류가 난다.
                      // 그래서 Long 즉, 참조타입으로 작성해야 null값을 허용해서 값이 제대로 들어가게 된다.

    @Column(length = 10, nullable = false)
    private String metaNickname;

    @Column(unique = true, nullable = false)
    private Long metaIdx;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DTO 구역

    // 입장한 방 Request DTO
    @Getter
    @Setter
    @ToString
    public static class rqEnterMeta {
        // DTO를 Entity로 변환 (빌더 방식) - 스터디룸, 카페룸
        public EnterMeta toEntity(String nickname, long idx) {
            return EnterMeta.builder()
                    .idx(null) // MySQL에서 AUTO_INCREMENT를 사용하면 null값이 들어가야 자동으로 숫자가 올라간다.
                    .metaNickname(nickname)
                    .metaIdx(idx)
                    .build();
        }
    }
}
