package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "RecruitStudy")
public class RecruitStudy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 20, nullable = false)
    private String writeDate;

    @Column(length = 20, nullable = false)
    private String writer;

    @Column(length = 20, nullable = false)
    private String studyType;

    @Column(nullable = false)
    private int personnel;

    @Column(nullable = false)
    private int recruitingPersonnel;

    @Column(nullable = false)
    private int recruiting;

    @Column(length = 50, nullable = false)
    private String image;

    @Column(length = 500, nullable = false)
    private String studyIntro;

    @Column(nullable = false)
    private int studyLike;

    @Column(nullable = false)
    private int studyLikeCheck;
    //0이면 좋아요 안함
    //1이면 좋아요 누름

}
