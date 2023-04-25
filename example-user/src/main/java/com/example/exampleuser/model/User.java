package com.example.exampleuser.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户表
* @since seeingflow
*/
@Data
@TableName("user")
@ApiModel(value = "User对象", description = "用户表")
public class User {


    @ApiModelProperty(value = "用户ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @ApiModelProperty(value = "用户姓名")
    @TableField("user_name")
    private String userName;

    @ApiModelProperty(value = "用户密码")
    @TableField("user_password")
    private String userPassword;

    @ApiModelProperty(value = "手机号")
    @TableField("user_mobile")
    private String userMobile;

    @ApiModelProperty(value = "积分")
    @TableField("user_score")
    private Integer userScore;

    @ApiModelProperty(value = "注册时间")
    @TableField("user_reg_time")
    private Date userRegTime;

    @ApiModelProperty(value = "用户余额")
    @TableField("user_money")
    private BigDecimal userMoney;



}