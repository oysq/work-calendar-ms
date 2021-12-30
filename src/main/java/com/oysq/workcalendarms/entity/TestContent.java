package com.oysq.workcalendarms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("test_content")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestContent {

    private int id;

    private String content;

}
