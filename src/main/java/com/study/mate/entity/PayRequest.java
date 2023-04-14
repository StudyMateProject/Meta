package com.study.mate.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
public class PayRequest {
    //제이슨 형식으로온 객체를 저장하기 위해서 사용
    private String imp_uid;
    private String merchant_uid;
}
