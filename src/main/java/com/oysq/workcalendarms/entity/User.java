package com.oysq.workcalendarms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@TableName("user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @TableId
    @TableField("user_id")
    private String userId;

    @TableField("user_name")
    private String userName;

    @TableField("password")
    private String password;

    @TableField("create_time")
    private String createTime;

    /** token **/

    @TableField("token")
    private String token;

    @TableField("build_time")
    private Long buildTime;

    @TableField("overdue_time")
    private Long overdueTime;

    /** 岗位薪资 **/
    @TableField("post_salary")
    private BigDecimal postSalary;


}
