package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
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
* @Desc MQ消息生产表
* @since seeingflow
*/
@Data
@TableName("producer_msg")
@ApiModel(value = "ProducerMsg对象", description = "MQ消息生产表")
public class ProducerMsg {
    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty(value = "生产者组名")
    @TableField("group_name")
    private String groupName;

    @ApiModelProperty(value = "消息主题")
    @TableField("msg_topic")
    private String msgTopic;

    @ApiModelProperty(value = "Tag")
    @TableField("msg_tag")
    private String msgTag;

    @ApiModelProperty(value = "Key")
    @TableField("msg_key")
    private String msgKey;

    @ApiModelProperty(value = "消息内容")
    @TableField("msg_body")
    private String msgBody;

    @ApiModelProperty(value = "0:未处理;1:已经处理")
    @TableField("msg_status")
    private Integer msgStatus;

    @ApiModelProperty(value = "记录时间")
    @TableField("create_time")
    private Date createTime;



}