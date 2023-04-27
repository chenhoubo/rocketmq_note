package com.example.shop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.ProducerMsg;
import org.apache.ibatis.annotations.Mapper;
/**
* @author seeingflow
* @Date 2023-04-25
* @Desc MQ消息生产表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface ProducerMsgMapper extends BaseMapper<ProducerMsg> {

}
