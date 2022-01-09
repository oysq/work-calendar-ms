package com.oysq.workcalendarms.constant;

import cn.hutool.core.collection.CollUtil;

import java.math.BigDecimal;
import java.util.List;

public class GlobalConstant {

    /**
     * 时间分割线：八点半之前算昨天，之后算今天
     */
    public static final String RANGE_LINE_TIME = "08:30:00";

    /**
     * 默认开始计算时间
     */
    public static final String DEFAULT_START_TIME = "18:00:00";

    /**
     * 默认倍率
     */
    public static final BigDecimal DEFAULT_MULTIPLY_RATE = new BigDecimal("1.5");

    /**
     * 倍率列表
     */
    public static final List<String> MULTIPLY_RATE_LIST = CollUtil.newArrayList("1.5", "2.0", "3.0");


//    调整当天开始时间、设置当天倍率、设置岗位薪资
//    g=3960
//    ((g/21.78)/8)*1.5
//              周末*2
//         法定节假日*3
//    排除：12:00-1:00
//    排除：5:30-6:00

}
