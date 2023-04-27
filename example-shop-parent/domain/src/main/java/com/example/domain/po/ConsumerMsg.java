package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc MQ消息消费表
* @since seeingflow
*/
@Data
@TableName("consumer_msg")
@ApiModel(value = "ConsumerMsg对象", description = "MQ消息消费表")
public class ConsumerMsg {


    @ApiModelProperty(value = "消息ID")
    @TableId("msg_id")
    private String msgId;

    @ApiModelProperty(value = "消费者组名")
    @TableField("group_name")
    private String groupName;

    @ApiModelProperty(value = "Tag")
    @TableField("msg_tag")
    private String msgTag;

    @ApiModelProperty(value = "Key")
    @TableField("msg_key")
    private String msgKey;

    @ApiModelProperty(value = "消息体")
    @TableField("msg_body")
    private String msgBody;

    @ApiModelProperty(value = "0:正在处理;1:处理成功;2:处理失败")
    @TableField("consumer_status")
    private Integer consumerStatus;

    @ApiModelProperty(value = "消费次数")
    @TableField("consumer_times")
    private Integer consumerTimes;

    @ApiModelProperty(value = "消费时间")
    @TableField("consumer_timestamp")
    private Date consumerTimestamp;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;



}