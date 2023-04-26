package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单商品日志表
* @since seeingflow
*/
@Data
@TableName("goods_order_log")
@ApiModel(value = "GoodsOrderLog对象", description = "订单商品日志表")
public class GoodsOrderLog {


    @ApiModelProperty(value = "商品ID")
    @TableField("goods_id")
    private Long goodsId;

    @ApiModelProperty(value = "订单ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "库存数量")
    @TableField("goods_number")
    private Integer goodsNumber;

    @ApiModelProperty(value = "记录时间")
    @TableField("log_time")
    private Date logTime;



}