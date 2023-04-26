package org.apache.rocketmq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.GoodsOrderLog;
import org.apache.ibatis.annotations.Mapper;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单商品日志表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface GoodsOrderLogMapper extends BaseMapper<GoodsOrderLog> {

}
