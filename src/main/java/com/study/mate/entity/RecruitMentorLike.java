package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "RecruitMentorLike")
public class RecruitMentorLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private Long likeIdx;

    @Column(nullable = false)
    private Long memberIdx;
}
