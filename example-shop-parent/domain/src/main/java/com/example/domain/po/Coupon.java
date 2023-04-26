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
* @Desc 优惠券表
* @since seeingflow
*/
@Data
@TableName("coupon")
@ApiModel(value = "Coupon对象", description = "优惠券表")
public class Coupon {


    @ApiModelProperty(value = "优惠券ID")
    @TableId(value = "coupon_id", type = IdType.AUTO)
    private Long couponId;

    @ApiModelProperty(value = "优惠券金额")
    @TableField("coupon_price")
    private BigDecimal couponPrice;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "订单ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "是否使用 0未使用 1已使用")
    @TableField("is_used")
    private Integer isUsed;

    @ApiModelProperty(value = "使用时间")
    @TableField("used_time")
    private Date usedTime;



}