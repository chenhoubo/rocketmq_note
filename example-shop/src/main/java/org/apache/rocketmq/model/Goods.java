package org.apache.rocketmq.model;

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
* @Desc 商品表
* @since seeingflow
*/
@Data
@TableName("goods")
@ApiModel(value = "Goods对象", description = "商品表")
public class Goods {


    @ApiModelProperty(value = "主键")
    @TableId(value = "goods_id", type = IdType.AUTO)
    private Long goodsId;

    @ApiModelProperty(value = "商品名称")
    @TableField("goods_name")
    private String goodsName;

    @ApiModelProperty(value = "商品库存")
    @TableField("goods_number")
    private Integer goodsNumber;

    @ApiModelProperty(value = "商品价格")
    @TableField("goods_price")
    private BigDecimal goodsPrice;

    @ApiModelProperty(value = "商品描述")
    @TableField("goods_desc")
    private String goodsDesc;

    @ApiModelProperty(value = "添加时间")
    @TableField("add_time")
    private Date addTime;

    @ApiModelProperty(value = "修改时间")
    @TableField("update_time")
    private Date updateTime;



}