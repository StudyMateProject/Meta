package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "RecruitMentorComment")
public class RecruitMentorComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private Long commentIdx;

    @Column(nullable = false, length = 10)
    private String writeDate;

    @Column(nullable = false, length = 50)
    private String writer;

    @Column(nullable = false, length = 500)
    private String comment;

    @Column(nullable = false)
    private int deleteCheck;

}
