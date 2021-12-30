package com.oysq.workcalendarms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.oysq.workcalendarms.mapper")
public class WorkCalendarMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkCalendarMsApplication.class, args);
    }

}
