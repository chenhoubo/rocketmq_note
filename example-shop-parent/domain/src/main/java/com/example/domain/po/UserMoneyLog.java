package com.example.domain.po;

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
* @Desc 用户余额日志表
* @since seeingflow
*/
@Data
@TableName("user_money_log")
@ApiModel(value = "UserMoneyLog对象", description = "用户余额日志表")
public class UserMoneyLog {


    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "订单ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "日志类型 1订单付款 2 订单退款")
    @TableField("money_log_type")
    private Integer moneyLogType;

    @ApiModelProperty(value = "操作金额")
    @TableField("use_money")
    private BigDecimal useMoney;

    @ApiModelProperty(value = "日志时间")
    @TableField("create_time")
    private Date createTime;



}