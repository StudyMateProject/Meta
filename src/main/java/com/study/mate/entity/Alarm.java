package com.study.mate.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "Alarm")
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 50, nullable = false)
    private String emailId;

    @Column(nullable = false)
    private int alarmType;
    //좋아요 : 1
    //신청 : 2
    //1대1 채팅 : 3
    //댓글 : 4

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 100, nullable = false)
    private String title;

    @Column()
    private Long recruitStudyIdx;

    @Column()
    private Long recruitMentorIdx;

    @Column()
    private Long recruitMenteeIdx;
}
