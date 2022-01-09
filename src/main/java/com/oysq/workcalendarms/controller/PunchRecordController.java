package com.oysq.workcalendarms.controller;

import com.oysq.workcalendarms.entity.PunchRecord;
import com.oysq.workcalendarms.entity.Res;
import com.oysq.workcalendarms.service.PunchRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/punchRecord")
public class PunchRecordController {

    @Autowired
    private PunchRecordService punchRecordService;

    /**
     * 调整开始时间
     */
    @PostMapping("updateStartTime")
    public Res updateStartTime(@RequestBody PunchRecord record) {
        // TODO 校验Token/SpringSecurity
        try {
            punchRecordService.updateStartTime(record);
            return Res.success("更新成功", null);
        } catch (Exception e) {
            log.error("更新开始时间失败", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 调整结束时间
     */
    @PostMapping("updateEndTime")
    public Res updateEndTime(@RequestBody PunchRecord record) {
        // TODO 校验Token/SpringSecurity
        try {
            punchRecordService.updateEndTime(record);
            return Res.success("打卡成功", null);
        } catch (Exception e) {
            log.error("更新结束时间失败", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 调整倍率
     */
    @PostMapping("updateMultiplyRate")
    public Res updateMultiplyRate(@RequestBody PunchRecord record) {
        // TODO 校验Token/SpringSecurity
        try {
            punchRecordService.updateMultiplyRate(record);
            return Res.success("更新成功", null);
        } catch (Exception e) {
            log.error("调整倍率失败", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 查询打卡记录
     */
    @PostMapping("selectRecord")
    public Res selectRecord(@RequestBody Map<String, String> param) {
        // TODO 校验Token/SpringSecurity
        try {
            return Res.success(punchRecordService.selectRecord(param.get("userId"), param.get("startDate"), param.get("endDate")));
        } catch (Exception e) {
            log.error("查询打卡记录失败", e);
            return Res.fail(e.getMessage());
        }
    }


}
