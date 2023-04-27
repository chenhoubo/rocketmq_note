package com.example.domain.to;

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
@ApiModel(description = "订单To")
public class ConfirmOrderTo {


    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "订单状态 0未确认 1已确认 2已取消 3无效 4退款")
    private Integer orderStatus;

    @ApiModelProperty(value = "支付状态 0未支付 1支付中 2已支付")
    private Integer payStatus;

    @ApiModelProperty(value = "发货状态 0未发货 1已发货 2已退货")
    private Integer shippingStatus;

    @ApiModelProperty(value = "收货地址")
    private String address;

    @ApiModelProperty(value = "收货人")
    private String consignee;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "商品数量")
    private Integer goodsNumber;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal goodsPrice;

    @ApiModelProperty(value = "商品总价")
    private BigDecimal goodsAmount;

    @ApiModelProperty(value = "运费")
    private BigDecimal shippingFee;

    @ApiModelProperty(value = "订单价格")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "优惠券ID")
    private Long couponId;

    @ApiModelProperty(value = "优惠券")
    private BigDecimal couponPaid;

    @ApiModelProperty(value = "已付金额")
    private BigDecimal moneyPaid;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "创建时间")
    private Date addTime;

    @ApiModelProperty(value = "订单确认时间")
    private Date confirmTime;

    @ApiModelProperty(value = "支付时间")
    private Date payTime;



}