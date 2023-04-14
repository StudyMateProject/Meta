package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "RecruitStudyLike")
public class RecruitStudyLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private Long likeIdx;
    //나중에 포린키로 연결하고 지금은 그냥 저장 하는거로

    @Column(nullable = false)
    private Long memberIdx;
    //이것도 나중에 포린키로 연결

}
