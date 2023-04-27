package com.example.domain.to;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 10:54
 * @Desc
 * @since seeingflow
 */
@Data
@ApiModel(description = "CancelOrderMQ")
public class CancelOrderMQ extends MQEntity{

    @ApiModelProperty(value = "积分")
    private Integer userScore;

}
