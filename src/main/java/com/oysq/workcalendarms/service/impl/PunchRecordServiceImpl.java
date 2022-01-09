package com.oysq.workcalendarms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oysq.workcalendarms.constant.GlobalConstant;
import com.oysq.workcalendarms.entity.PunchRecord;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.mapper.PunchRecordMapper;
import com.oysq.workcalendarms.service.PunchRecordService;
import com.oysq.workcalendarms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@Service
public class PunchRecordServiceImpl implements PunchRecordService {

    @Autowired
    private UserService userService;

    @Autowired
    private PunchRecordMapper punchRecordMapper;

    /**
     * 构建并获取一条打卡数据
     */
    private PunchRecord getPunch(String userId, String punchDate) {
        if (StrUtil.hasBlank(userId, punchDate)) {
            throw new RuntimeException("主键参数缺失");
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
        PunchRecord dbRecord = getPunch(user.getUserId(), record.getPunchDate());
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
        PunchRecord dbRecord = getPunch(user.getUserId(), record.getPunchDate());
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
        if (!GlobalConstant.MULTIPLY_RATE_LIST.contains(record.getMultiplyRate().toString())) {
            throw new RuntimeException("倍率不存在");
        }

        // 获取用户
        User user = userService.selectByUserId(record.getUserId());
        if (null == user) {
            throw new RuntimeException("用户查询失败");
        }

        // 得到记录
        PunchRecord dbRecord = getPunch(user.getUserId(), record.getPunchDate());

        // 更新数据
        dbRecord.setMultiplyRate(record.getMultiplyRate());
        dbRecord.setPostSalary(user.getPostSalary());
        PunchRecord.calcOvertime(dbRecord);

        // 入库
        updatePunch(dbRecord);

    }

    @Override
    public List<PunchRecord> selectRecord(String userId, String startDate, String endDate) {

        // 基础校验
        if (StrUtil.hasBlank(userId, startDate, endDate)) {
            throw new RuntimeException("参数缺失");
        }

        // 查询并返回
        return punchRecordMapper.selectList(
                new LambdaQueryWrapper<PunchRecord>()
                        .eq(PunchRecord::getUserId, userId)
                        .ge(PunchRecord::getPunchDate, startDate)
                        .le(PunchRecord::getPunchDate, endDate)
                        .orderByAsc(PunchRecord::getPunchDate)
        );
    }

}
