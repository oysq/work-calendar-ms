package com.oysq.workcalendarms.service;

import com.oysq.workcalendarms.entity.PunchRecord;

import java.util.List;

public interface PunchRecordService {

    /**
     * 查询打卡记录
     *
     * @param startDate yyyy/MM/dd
     * @param endDate   yyyy/MM/dd
     */
    List<PunchRecord> selectRecord(String userId, String startDate, String endDate);

    /**
     * 更新开始时间
     */
    void updateStartTime(PunchRecord record);

    /**
     * 更新结束时间
     */
    void updateEndTime(PunchRecord record);

    /**
     * 更新倍率
     */
    void updateMultiplyRate(PunchRecord record);

    /**
     * 删除打卡记录
     */
    void delete(PunchRecord record);


}
