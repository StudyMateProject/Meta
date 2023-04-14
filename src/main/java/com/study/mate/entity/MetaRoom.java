package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
@Entity(name = "MetaRoom") // Entity 어노테이션 - 괄호안에는 테이블명과 똑같이 작성한다.
public class MetaRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private long metaIdx;

    @Column(length = 20)
    private String metaNickname;

    @Column(length = 100)
    private String metaProfileImage;

    @Column (length = 20)
    private String metaMaster;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DTO 구역

    // 방 번호에 해당하는 방 참가자들 조회 Response DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rpMetaRoomIdxList {
        private String metaNickname;
        private String metaProfileImage;
        private String metaMaster;

        // Entity를 DTO로 변환 (생성자 방식)
        public rpMetaRoomIdxList(MetaRoom metaRoom) {
            this.metaNickname = metaRoom.getMetaNickname();
            this.metaProfileImage = metaRoom.getMetaProfileImage();
            this.metaMaster = metaRoom.getMetaMaster();
        }
    }

    // 방 번호에 해당하는 방 참가 상태 체크 Response DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rpEntranceCheck {
        private String metaNickname;

        // Entity를 DTO로 변환 (생성자 방식)
        public rpEntranceCheck(MetaRoom metaRoom) {
            this.metaNickname = metaRoom.getMetaNickname();
        }
    }
}
