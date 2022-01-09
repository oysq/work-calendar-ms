package com.oysq.workcalendarms.entity;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oysq.workcalendarms.constant.GlobalConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@TableName("punch_record")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PunchRecord {

    @TableId
    @TableField("punch_id")
    private String punchId;

    @TableField("user_id")
    private String userId;

    /**
     * 所属日期 兼容IOS写法：2021/01/08
     **/
    @TableField("punch_date")
    private String punchDate;

    /**
     * 开始时间 yyyy/MM/dd HH:mm:ss
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 结束时间 yyyy/MM/dd HH:mm:ss
     */
    @TableField("end_time")
    private String endTime;

    /**
     * 岗位薪资
     **/
    @TableField("post_salary")
    private BigDecimal postSalary;

    /**
     * 倍率：1.5/2.0/3.0
     */
    @TableField("multiply_rate")
    private BigDecimal multiplyRate;

    /**
     * 加班工资
     */
    @TableField("overtime_pay")
    private BigDecimal overtimePay;


    /**
     * 检验非空参数
     */
    public static void checkBlank(PunchRecord record) {
        if (null == record) {
            throw new RuntimeException("参数不可空");
        }
        if (StrUtil.isBlank(record.getUserId())) {
            throw new RuntimeException("用户ID不可空");
        }
        if (StrUtil.isBlank(record.getPunchDate())) {
            throw new RuntimeException("打卡日期不可空");
        }
    }

    /**
     * 校验时间合法
     */
    public static void checkLegal(PunchRecord record) {
        // 校验时间范围
        DateTime startLine = DateUtil.parse(record.getPunchDate() + " " + GlobalConstant.RANGE_LINE_TIME, "yyyy/MM/dd HH:mm:ss");
        DateTime endLine = DateUtil.offsetDay(startLine, 1);
        // 实际提交的时间范围
        DateTime startTime = DateUtil.parse(record.getStartTime(), "yyyy/MM/dd HH:mm:ss");
        DateTime endTime = StrUtil.isNotBlank(record.getEndTime()) ?
                DateUtil.parse(record.getEndTime(), "yyyy/MM/dd HH:mm:ss")
                : null;
        // 判断时间正确性
        if (null != endTime && startTime.isAfterOrEquals(endTime)) {
            throw new RuntimeException("结束时间应晚于开始时间");
        }
        if (startTime.isBefore(startLine)) {
            throw new RuntimeException("开始时间太早，请拆分为两天打卡");
        }
        if (null != endTime && endTime.isAfter(endLine)) {
            throw new RuntimeException("结束时间太晚，请拆分为两天打卡");
        }
    }

    /**
     * 计算加班费
     */
    public static BigDecimal calcOvertimePay(PunchRecord record) {
        if (null == record
                || StrUtil.hasBlank(record.getStartTime(), record.getEndTime())
                || null == record.getPostSalary()
                || null == record.getMultiplyRate()) {
            return BigDecimal.ZERO;
        }
        DateTime startTime = DateUtil.parse(record.getStartTime(), "yyyy/MM/dd HH:mm:ss");
        DateTime endTime = DateUtil.parse(record.getEndTime(), "yyyy/MM/dd HH:mm:ss");

        return BigDecimal.ZERO;

    }


}
