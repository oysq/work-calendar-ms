package com.oysq.workcalendarms.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
     * 加班时长
     */
    @TableField("overtime_duration")
    private BigDecimal overtimeDuration;

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
    public static void calcOvertime(PunchRecord record) {
        if (null == record
                || StrUtil.hasBlank(record.getPunchDate(), record.getStartTime(), record.getEndTime())
                || null == record.getPostSalary()
                || null == record.getMultiplyRate()) {
            return;
        }

        // 真实时间
        DateTime startTime = DateUtil.parse(record.getStartTime(), "yyyy/MM/dd HH:mm:ss").setField(DateField.SECOND, 0);
        DateTime endTime = DateUtil.parse(record.getEndTime(), "yyyy/MM/dd HH:mm:ss").setField(DateField.SECOND, 0);

        // 取整时间
        if (startTime.getField(DateField.MINUTE) != 0 && startTime.getField(DateField.MINUTE) != 30) {
            if(startTime.isBefore(new DateTime(startTime).setField(DateField.MINUTE, 30))) {
                startTime.setField(DateField.MINUTE, 30);
            } else {
                startTime.setField(DateField.MINUTE, 0);
                startTime = DateUtil.offsetHour(startTime, 1);
            }
        }
        if (endTime.getField(DateField.MINUTE) != 0 && endTime.getField(DateField.MINUTE) != 30) {
            if(endTime.isBefore(new DateTime(endTime).setField(DateField.MINUTE, 30))) {
                endTime.setField(DateField.MINUTE, 0);
            } else {
                endTime.setField(DateField.MINUTE, 30);
            }
        }

        // 获取半小时列表
        List<DateTime> rangeList = DateUtil.rangeToList(startTime, endTime, DateField.MINUTE, 30);
        if(CollUtil.isNotEmpty(rangeList)) {
            // 以开始时间标记每个半小时，所以抹除最后一个时刻
            rangeList.remove(rangeList.size()-1);
            if(CollUtil.isNotEmpty(rangeList)) {
                // 抹除两个区间
                ArrayList<DateTime> excludeList = CollUtil.newArrayList(
                        DateUtil.parse(record.getPunchDate() + " 12:00:00", "yyyy/MM/dd HH:mm:ss"),
                        DateUtil.parse(record.getPunchDate() + " 12:30:00", "yyyy/MM/dd HH:mm:ss"),
                        DateUtil.parse(record.getPunchDate() + " 17:30:00", "yyyy/MM/dd HH:mm:ss")
                );
                rangeList.removeAll(excludeList);
            }
        }

        // 得到加班时长
        record.setOvertimeDuration(new BigDecimal(rangeList.size()).multiply(new BigDecimal("0.5")));

        // 累计满1小时才有加班工资
        if(record.getOvertimeDuration().compareTo(BigDecimal.ONE) >= 0) {
            // 得到加班工资，保留两位小数
            // ((g/21.78)/8)*1.5
            BigDecimal res1 = record.getPostSalary().divide(new BigDecimal("21.78"), 6, RoundingMode.HALF_UP);
            BigDecimal res2 = res1.divide(new BigDecimal("8"), 6, RoundingMode.HALF_UP);
            BigDecimal res3 = res2.multiply(record.getMultiplyRate()).setScale(6, RoundingMode.HALF_UP);
            BigDecimal overtimePay = res3.multiply(record.getOvertimeDuration()).setScale(2, RoundingMode.HALF_UP);
            record.setOvertimePay(overtimePay);
        } else {
            record.setOvertimePay(BigDecimal.ZERO);
        }


    }


}
