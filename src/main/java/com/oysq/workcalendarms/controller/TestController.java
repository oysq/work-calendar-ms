package com.oysq.workcalendarms.controller;

import com.oysq.workcalendarms.entity.TestContent;
import com.oysq.workcalendarms.mapper.TestContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestContentMapper testContentMapper;

    @GetMapping("/abc")
    public String test() {
        testContentMapper.insert(
                TestContent.builder().content("测试一下").build()
        );
        return "oysq";
    }

}
