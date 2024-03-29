package com.oysq.workcalendarms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oysq.workcalendarms.constant.GlobalConstant;
import com.oysq.workcalendarms.entity.PunchRecord;
import com.oysq.workcalendarms.entity.PunchReport;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.mapper.PunchRecordMapper;
import com.oysq.workcalendarms.service.PunchRecordService;
import com.oysq.workcalendarms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Transactional
@Service
public class PunchRecordServiceImpl implements PunchRecordService {

    @Autowired
    private UserService userService;

    @Autowired
    private PunchRecordMapper punchRecordMapper;

    @Override
    public List<PunchRecord> selectRecord(String userId, String startDate, String endDate) {

        // 基础校验
        if (StrUtil.hasBlank(userId)) {
            throw new RuntimeException("参数缺失");
        }

        // 构造查询
        LambdaQueryWrapper<PunchRecord> queryWrapper = new LambdaQueryWrapper<PunchRecord>()
                .eq(PunchRecord::getUserId, userId)
                .orderByAsc(PunchRecord::getPunchDate);

        if (StrUtil.isNotBlank(startDate)) {
            queryWrapper.ge(PunchRecord::getPunchDate, startDate);
        }
        if (StrUtil.isNotBlank(endDate)) {
            queryWrapper.le(PunchRecord::getPunchDate, endDate);
        }

        // 查询并返回
        return punchRecordMapper.selectList(queryWrapper);
    }

    @Override
    public PunchReport selectReport(String userId, String startDate, String endDate) {

        // 查询
        List<PunchRecord> records = this.selectRecord(userId, startDate, endDate);

        // 待返回数据
        PunchReport report = PunchReport.builder()
                .userId(userId)
                .dayNum(0L)
                .overtime(BigDecimal.ZERO)
                .overtimeWorkDay(BigDecimal.ZERO)
                .overtimeNonWorkDay(BigDecimal.ZERO)
                .overtimePay(BigDecimal.ZERO)
                .overtimePayWorkDay(BigDecimal.ZERO)
                .overtimePayNonWorkDay(BigDecimal.ZERO)
                .build();

        // 计算
        if (CollUtil.isNotEmpty(records)) {
            report.setDayNum(
                    records.stream()
                            .filter(item ->
                                    Objects.nonNull(item.getStartTime())
                                    && Objects.nonNull(item.getEndTime())
                            )
                            .count()
            );
            report.setOvertime(
                    records.stream()
                            .map(PunchRecord::getOvertimeDuration)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal::add)
                            .orElse(BigDecimal.ZERO)
            );
            report.setOvertimeWorkDay(
                    records.stream()
                            .filter(item -> null != item.getOvertimeDuration()
                                    && GlobalConstant.DEFAULT_MULTIPLY_RATE.compareTo(item.getMultiplyRate()) == 0)
                            .map(PunchRecord::getOvertimeDuration)
                            .reduce(BigDecimal::add)
                            .orElse(BigDecimal.ZERO)
            );
            report.setOvertimeNonWorkDay(
                    report.getOvertime().subtract(report.getOvertimeWorkDay())
            );
            report.setOvertimePay(
                    records.stream()
                            .map(PunchRecord::getOvertimePay)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal::add)
                            .orElse(BigDecimal.ZERO)
            );
            report.setOvertimePayWorkDay(
                    records.stream()
                            .filter(item -> null != item.getOvertimePay()
                                    && GlobalConstant.DEFAULT_MULTIPLY_RATE.compareTo(item.getMultiplyRate()) == 0)
                            .map(PunchRecord::getOvertimePay)
                            .reduce(BigDecimal::add)
                            .orElse(BigDecimal.ZERO)
            );
            report.setOvertimePayNonWorkDay(
                    report.getOvertimePay().subtract(report.getOvertimePayWorkDay())
            );
        }

        return report;
    }

    /**
     * 获取一条打卡数据，不存在则创建入库
     */
    private PunchRecord getPunchOrInsert(String userId, String punchDate) {
        if (StrUtil.hasBlank(userId, punchDate)) {
            throw new RuntimeException("参数信息缺失");
        }
        List<PunchRecord> punchRecords = punchRecordMapper.selectList(
                new LambdaQueryWrapper<PunchRecord>()
                        .eq(PunchRecord::getUserId, userId)
                        .eq(PunchRecord::getPunchDate, punchDate)
        );
        if (CollUtil.isNotEmpty(punchRecords)) {
            if (punchRecords.size() > 1) {
                throw new RuntimeException("数据异常，请联系管理员检查");
            }
            return punchRecords.get(0);
        } else {
            PunchRecord newRecord = PunchRecord.builder()
                    .punchId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .userId(userId)
                    .punchDate(punchDate)
                    .startTime(punchDate + " " + GlobalConstant.DEFAULT_START_TIME)    // 默认开始时间
                    .multiplyRate(GlobalConstant.DEFAULT_MULTIPLY_RATE)             // 默认倍率
                    .build();
            if (punchRecordMapper.insert(newRecord) <= 0) {
                throw new RuntimeException("打卡创建失败");
            }
            return newRecord;
        }
    }

    /**
     * 获取一条打卡数据，不存在则抛出异常
     */
    private PunchRecord getPunchOrError(String userId, String punchDate) {
        if (StrUtil.hasBlank(userId, punchDate)) {
            throw new RuntimeException("参数信息缺失");
        }
        List<PunchRecord> punchRecords = punchRecordMapper.selectList(
                new LambdaQueryWrapper<PunchRecord>()
                        .eq(PunchRecord::getUserId, userId)
                        .eq(PunchRecord::getPunchDate, punchDate)
        );
        if (CollUtil.isNotEmpty(punchRecords)) {
            if (punchRecords.size() > 1) {
                throw new RuntimeException("数据异常，请联系管理员检查");
            }
            return punchRecords.get(0);
        }
        throw new RuntimeException("打卡信息不存在");
    }

    /**
     * 更新记录
     */
    private void updatePunch(PunchRecord punchRecord) {
        if (StrUtil.isBlank(punchRecord.getPunchId())) {
            throw new RuntimeException("Punch主键ID不可空");
        }
        if (punchRecordMapper.updateById(punchRecord) < 0) {
            throw new RuntimeException("打卡更新失败");
        }
    }

    @Override
    public void updateStartTime(PunchRecord record) {

        // 非空校验
        PunchRecord.checkBlank(record);

        // 业务校验
        if (StrUtil.isBlank(record.getStartTime())) {
            throw new RuntimeException("开始时间不可空");
        }

        // 获取用户
        User user = userService.selectByUserId(record.getUserId());
        if (null == user) {
            throw new RuntimeException("用户查询失败");
        }

        // 得到记录
        PunchRecord dbRecord = getPunchOrInsert(user.getUserId(), record.getPunchDate());
        record.setEndTime(dbRecord.getEndTime());

        // 校验时间合法
        PunchRecord.checkLegal(record);

        // 更新数
        dbRecord.setStartTime(record.getStartTime());
        dbRecord.setPostSalary(user.getPostSalary());
        PunchRecord.calcOvertime(dbRecord);

        // 入库
        updatePunch(dbRecord);

    }

    @Override
    public void updateEndTime(PunchRecord record) {

        // 非空校验
        PunchRecord.checkBlank(record);

        // 业务校验
        if (StrUtil.isBlank(record.getEndTime())) {
            throw new RuntimeException("打卡时间不可空");
        }

        // 获取用户
        User user = userService.selectByUserId(record.getUserId());
        if (null == user) {
            throw new RuntimeException("用户查询失败");
        }

        // 得到记录
        PunchRecord dbRecord = getPunchOrInsert(user.getUserId(), record.getPunchDate());
        record.setStartTime(dbRecord.getStartTime());

        // 校验时间合法
        PunchRecord.checkLegal(record);

        // 更新数据
        dbRecord.setEndTime(record.getEndTime());
        dbRecord.setPostSalary(user.getPostSalary());
        PunchRecord.calcOvertime(dbRecord);

        // 入库
        updatePunch(dbRecord);

    }

    @Override
    public void updateMultiplyRate(PunchRecord record) {

        // 非空校验
        PunchRecord.checkBlank(record);

        // 业务校验
        if (record.getMultiplyRate() == null) {
            throw new RuntimeException("倍率不可空");
        }
        if (GlobalConstant.MULTIPLY_RATE_LIST.stream().noneMatch(item -> new BigDecimal(item).compareTo(record.getMultiplyRate()) == 0)) {
            throw new RuntimeException("倍率不存在");
        }

        // 获取用户
        User user = userService.selectByUserId(record.getUserId());
        if (null == user) {
            throw new RuntimeException("用户查询失败");
        }

        // 得到记录
        PunchRecord dbRecord = getPunchOrInsert(user.getUserId(), record.getPunchDate());

        // 更新数据
        dbRecord.setMultiplyRate(record.getMultiplyRate());
        dbRecord.setPostSalary(user.getPostSalary());
        PunchRecord.calcOvertime(dbRecord);

        // 入库
        updatePunch(dbRecord);

    }

    @Override
    public void delete(PunchRecord record) {

        // 非空校验
        PunchRecord.checkBlank(record);

        // 获取用户
        User user = userService.selectByUserId(record.getUserId());
        if (null == user) {
            throw new RuntimeException("用户查询失败");
        }

        // 得到记录
        PunchRecord dbRecord = getPunchOrError(user.getUserId(), record.getPunchDate());

        // 删除
        if (punchRecordMapper.deleteById(dbRecord.getPunchId()) <= 0) {
            throw new RuntimeException("数据删除失败");
        }

    }


}
