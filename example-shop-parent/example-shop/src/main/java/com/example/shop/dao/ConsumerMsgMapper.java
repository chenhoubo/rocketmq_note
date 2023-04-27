package com.example.shop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.ConsumerMsg;
import org.apache.ibatis.annotations.Mapper;
/**
* @author seeingflow
* @Date 2023-04-25
* @Desc MQ消息消费表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface ConsumerMsgMapper extends BaseMapper<ConsumerMsg> {

}
