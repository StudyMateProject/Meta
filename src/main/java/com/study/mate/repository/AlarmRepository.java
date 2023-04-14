package com.study.mate.repository;

import com.study.mate.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Object> {
    //알림 타입과 이메일과 닉네임과 스터디 idx 로 검색
    Alarm findByAlarmTypeAndEmailIdAndNicknameAndRecruitStudyIdx(int alarmType, String emailId, String nickname, long recruitStudyIdx);
    //알림 타입과 이메일과 닉네임과 멘토 idx 로 검색
    Alarm findByAlarmTypeAndEmailIdAndNicknameAndRecruitMentorIdx(int alarmType, String emailId, String nickname, long recruitMentorIdx);
    //알림 타입과 이메일과 닉네임과 멘티 idx 로 검색
    Alarm findByAlarmTypeAndEmailIdAndNicknameAndRecruitMenteeIdx(int alarmType, String emailId, String nickname, long recruitMenteeIdx);
    //이메일로 알림 카운트
    int countByEmailId(String emailId);
    //이메일로 알림 리스트 검색
    List<Alarm> findByEmailId(String emailId);
    //idx(PrimaryKey) 로 알림 찾아오기
    Alarm findByIdx(long idx);
}
