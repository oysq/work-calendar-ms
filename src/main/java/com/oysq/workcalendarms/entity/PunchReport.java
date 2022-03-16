package com.oysq.workcalendarms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PunchReport {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 加班时长
     */
    private BigDecimal overtime;

    /**
     * 加班时长 - 工作日
     */
    private BigDecimal overtimeWorkDay;

    /**
     * 加班时长 - 非工作日
     */
    private BigDecimal overtimeNonWorkDay;

    /**
     * 加班工资
     */
    private BigDecimal overtimePay;


}
