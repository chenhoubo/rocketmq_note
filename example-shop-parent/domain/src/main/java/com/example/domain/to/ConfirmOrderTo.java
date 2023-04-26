package com.example.domain.to;

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
* @Desc 订单表
* @since seeingflow
*/
@Data
@TableName("order")
@ApiModel(value = "Order对象", description = "订单表")
public class ConfirmOrderTo {


    @ApiModelProperty(value = "订单ID")
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "订单状态 0未确认 1已确认 2已取消 3无效 4退款")
    @TableField("order_status")
    private Integer orderStatus;

    @ApiModelProperty(value = "支付状态 0未支付 1支付中 2已支付")
    @TableField("pay_status")
    private Integer payStatus;

    @ApiModelProperty(value = "发货状态 0未发货 1已发货 2已退货")
    @TableField("shipping_status")
    private Integer shippingStatus;

    @ApiModelProperty(value = "收货地址")
    @TableField("address")
    private String address;

    @ApiModelProperty(value = "收货人")
    @TableField("consignee")
    private String consignee;

    @ApiModelProperty(value = "商品ID")
    @TableField("goods_id")
    private Long goodsId;

    @ApiModelProperty(value = "商品数量")
    @TableField("goods_number")
    private Integer goodsNumber;

    @ApiModelProperty(value = "商品价格")
    @TableField("goods_price")
    private BigDecimal goodsPrice;

    @ApiModelProperty(value = "商品总价")
    @TableField("goods_amount")
    private BigDecimal goodsAmount;

    @ApiModelProperty(value = "运费")
    @TableField("shipping_fee")
    private BigDecimal shippingFee;

    @ApiModelProperty(value = "订单价格")
    @TableField("order_amount")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "优惠券ID")
    @TableField("coupon_id")
    private Long couponId;

    @ApiModelProperty(value = "优惠券")
    @TableField("coupon_paid")
    private BigDecimal couponPaid;

    @ApiModelProperty(value = "已付金额")
    @TableField("money_paid")
    private BigDecimal moneyPaid;

    @ApiModelProperty(value = "支付金额")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "创建时间")
    @TableField("add_time")
    private Date addTime;

    @ApiModelProperty(value = "订单确认时间")
    @TableField("confirm_time")
    private Date confirmTime;

    @ApiModelProperty(value = "支付时间")
    @TableField("pay_time")
    private Date payTime;



}