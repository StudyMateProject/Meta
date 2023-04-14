package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
@Entity(name = "Meta") // Entity 어노테이션 - 괄호안에는 테이블명과 똑같이 작성한다.
public class Meta {
    @Id // 기본키 어노테이션 - 기본키 설정 (PRIMARY KEY)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT - MySQL에서 시퀀스 역할을 담당한다.
    private Long idx; // MySQL에서 AUTO_INCREMENT를 사용하면 null값이 들어가야 자동으로 숫자가 올라간다.
                      // 하지만 long 즉, 원시타입으로 작성하면 null값을 허용하지 않기 때문에 오류가 난다.
                      // 그래서 Long 즉, 참조타입으로 작성해야 null값을 허용해서 값이 제대로 들어가게 된다.

    @Column(length = 50, nullable = false)
    private String metaTitle;

    @Column(length = 10, nullable = false)
    private String metaType;

    @Column(nullable = false)
    private int metaPersonnel;

    @Column(nullable = false)
    private int metaRecruitingPersonnel;

    @Column (length = 20)
    private String metaMaster;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DTO 구역

    // 생성된 메타 방 모두 조회 Response DTO
    @Getter // getter 어노테이션
    @Setter // setter 어노테이션
    @NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
    @AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
    @Builder // 빌더 사용 어노테이션
    @ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
    public static class rpMetaList {
        private long idx;
        private String metaTitle;
        private String metaType;
        private int metaPersonnel;
        private int metaRecruitingPersonnel;
        private String metaMaster;

        // Entity를 DTO로 변환 (생성자 방식)
        public rpMetaList(Meta meta) {
            this.idx = meta.getIdx();
            this.metaTitle = meta.getMetaTitle();
            this.metaType = meta.getMetaType();
            this.metaPersonnel = meta.getMetaPersonnel();
            this.metaRecruitingPersonnel = meta.getMetaRecruitingPersonnel();
            this.metaMaster = meta.getMetaMaster();
        }
    }

    // 방 만들기 Request DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rqCreateMeta {
        private String metaTitle;
        private String metaType;
        private int metaPersonnel;
        private String metaMaster;

        // DTO를 Entity로 변환 (빌더 방식) - 스터디룸, 카페룸
        public Meta toEntity() {
            return Meta.builder()
                    .idx(null) // MySQL에서 AUTO_INCREMENT를 사용하면 null값이 들어가야 자동으로 숫자가 올라간다.
                    .metaTitle(metaTitle)
                    .metaType(metaType)
                    .metaPersonnel(metaPersonnel)
                    .metaRecruitingPersonnel(1) // 방을 생성할때 참여중인 인원은 항상 1이다.
                    .metaMaster(metaMaster)
                    .build();
        }
        // DTO를 Entity로 변환 (빌더 방식) - 자습실
        public Meta toOneRoom() {
            return Meta.builder()
                    .idx(null) // MySQL에서 AUTO_INCREMENT를 사용하면 null값이 들어가야 자동으로 숫자가 올라간다.
                    .metaTitle("자습실")
                    .metaType("oneRoom")
                    .metaPersonnel(1)
                    .metaRecruitingPersonnel(1) // 방을 생성할때 참여중인 인원은 항상 0이다.
                    .metaMaster(metaMaster)
                    .build();
        }
    }

    // 방 만들기 Response DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rpCreateMeta {
        private long idx;
        private String metaType;


        // Entity를 DTO로 변환 (생성자 방식)
        public rpCreateMeta(Meta meta) {
            this.idx = meta.getIdx();
            this.metaType = meta.getMetaType();
        }
    }

    // 메타 방 이름 및 분류별 검색 Request DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rqSearchMetaList {
        private String searchType;
        private String search;
        private String metaType;

        // DTO를 Entity로 변환 (빌더 방식)
        public Meta toEntity() {
            if ( searchType.equals("idx") ) {
                return Meta.builder()
                        .idx(Long.valueOf(search))
                        .metaType(metaType)
                        .build();
            } else {
                return Meta.builder()
                        .metaTitle(search)
                        .metaType(metaType)
                        .build();
            }
        }
    }

    // 메타 방 이름 및 분류별 검색 Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rpSearchMetaList {
        private long idx;
        private String metaTitle;
        private String metaType;
        private int metaPersonnel;
        private int metaRecruitingPersonnel;
        private String metaMaster;

        // Entity를 DTO로 변환 (생성자 방식)
        public rpSearchMetaList(Meta meta) {
            this.idx = meta.getIdx();
            this.metaTitle = meta.getMetaTitle();
            this.metaType = meta.getMetaType();
            this.metaPersonnel = meta.getMetaPersonnel();
            this.metaRecruitingPersonnel = meta.getMetaRecruitingPersonnel();
            this.metaMaster = meta.getMetaMaster();
        }
    }

    // 입장한 메타 방 조회 Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rpEntrance {
        private long idx;
        private String metaTitle;
        private String metaType;
        private int metaPersonnel;
        private int metaRecruitingPersonnel;
        private String metaMaster;

        // Entity를 DTO로 변환 (생성자 방식)
        public rpEntrance(Meta meta) {
            this.idx = meta.getIdx();
            this.metaTitle = meta.getMetaTitle();
            this.metaType = meta.getMetaType();
            this.metaPersonnel = meta.getMetaPersonnel();
            this.metaRecruitingPersonnel = meta.getMetaRecruitingPersonnel();
            this.metaMaster = meta.getMetaMaster();
        }

        // 모집인원이 정원초과일 경우 에러메세지를 DTO로 변환 (생성자 방식)
        public rpEntrance(String err) { // 파라미터로 서비스에서 넘어온 에러 메시지를 받아온다.
            this.idx = 0; // idx는 0으로 고정해서 에러 체크값으로 사용한다.
            this.metaTitle = err; // metaTitle은 받아온 에러 메시지를 저장한다.
        }
    }
}
