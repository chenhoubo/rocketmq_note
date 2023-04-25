package org.apache.rocketmq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.rocketmq.model.GoodsOrderLog;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单商品日志表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface GoodsOrderLogMapper extends BaseMapper<GoodsOrderLog> {

}
