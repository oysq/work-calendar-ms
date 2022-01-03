package com.oysq.workcalendarms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Res {

    public Integer status;
    public String msg;
    public Object body;

    public static Res success(Object body) {
        return Res.builder().status(1).body(body).build();
    }

    public static Res success(String msg, Object body) {
        return Res.builder().status(1).msg(msg).body(body).build();
    }

    public static Res fail(String msg) {
        return Res.builder().status(0).msg(msg).build();
    }

}
