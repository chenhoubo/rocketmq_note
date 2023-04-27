package com.example.domain.to;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 11:52
 * @Desc
 * @since seeingflow
 */
@Data
@ApiModel(description = "MQEntity")
public class MQEntity {

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户余额")
    private BigDecimal userMoney;

    @ApiModelProperty(value = "优惠券ID")
    private Long couponId;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "商品数量")
    private Integer goodsNumber;

}
