package com.oysq.workcalendarms.controller;

import com.oysq.workcalendarms.entity.PunchRecord;
import com.oysq.workcalendarms.entity.Res;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.mapper.UserMapper;
import com.oysq.workcalendarms.service.PunchRecordService;
import com.oysq.workcalendarms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/punchRecord")
public class PunchRecordController {

    @Autowired
    private UserService userService;

    @Autowired
    private PunchRecordService punchRecordService;

    /**
     * 查询打卡记录
     */
    @PostMapping("selectRecord")
    public Res selectRecord(@RequestHeader(value = "C-TOKEN") String cToken, @RequestBody Map<String, String> param) {
        // TODO 校验Token/SpringSecurity
        try {
            userService.checkTokenSecurity(cToken, param);
            return Res.success(punchRecordService.selectRecord(param.get("userId"), param.get("startDate"), param.get("endDate")));
        } catch (Exception e) {
            log.error("查询打卡记录失败", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 调整开始时间
     */
    @PostMapping("updateStartTime")
    public Res updateStartTime(@RequestHeader(value = "C-TOKEN") String cToken, @RequestBody PunchRecord record) {
        // TODO 校验Token/SpringSecurity
        try {
            userService.checkTokenSecurity(cToken, record);
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
    public Res updateEndTime(@RequestHeader(value = "C-TOKEN") String cToken, @RequestBody PunchRecord record) {
        // TODO 校验Token/SpringSecurity
        try {
            userService.checkTokenSecurity(cToken, record);
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
    public Res updateMultiplyRate(@RequestHeader(value = "C-TOKEN") String cToken, @RequestBody PunchRecord record) {
        // TODO 校验Token/SpringSecurity
        try {
            userService.checkTokenSecurity(cToken, record);
            punchRecordService.updateMultiplyRate(record);
            return Res.success("更新成功", null);
        } catch (Exception e) {
            log.error("调整倍率失败", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 删除数据
     */
    @PostMapping("delete")
    public Res delete(@RequestHeader(value = "C-TOKEN") String cToken, @RequestBody PunchRecord record) {
        // TODO 校验Token/SpringSecurity
        try {
            userService.checkTokenSecurity(cToken, record);
            punchRecordService.delete(record);
            return Res.success("操作成功", null);
        } catch (Exception e) {
            log.error("数据删除失败", e);
            return Res.fail(e.getMessage());
        }
    }


}
