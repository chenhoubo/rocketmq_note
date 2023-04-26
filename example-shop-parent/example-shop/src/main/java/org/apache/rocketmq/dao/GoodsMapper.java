package org.apache.rocketmq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 商品表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    void subtractStock(@Param("goodsId") Long goodsId, @Param("number") Integer number);
}
