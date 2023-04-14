package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity(name = "RecruitStudyComment")
public class RecruitStudyComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private Long commentIdx;

    @Column(length = 10, nullable = false)
    private String writeDate;

    @Column(length = 50, nullable = false)
    private String writer;

    @Column(length = 500, nullable = false)
    private String comment;

    @Column(nullable = false)
    private int deleteCheck;
    //0은 삭제안됨
    //1은 삭제됨
}
