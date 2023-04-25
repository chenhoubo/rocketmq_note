package org.apache.rocketmq.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单支付表
* @since seeingflow
*/
@Data
@TableName("order_pay")
@ApiModel(value = "OrderPay对象", description = "订单支付表")
public class OrderPay {


    @ApiModelProperty(value = "支付编号")
    @TableField("pay_id")
    private Long payId;

    @ApiModelProperty(value = "订单编号")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "支付金额")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "是否已支付 1否 2是")
    @TableField("is_paid")
    private Integer isPaid;



}