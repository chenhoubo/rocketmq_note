package org.apache.rocketmq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.rocketmq.dao.GoodsMapper;
import org.apache.rocketmq.model.Goods;
import org.apache.rocketmq.service.GoodsService;
import org.apache.rocketmq.utils.ResultMsg;
import org.springframework.stereotype.Service;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 商品表 服务实现类
* @since seeingflow
*/
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Override
    public ResultMsg subtractStock(Long goodsId, Integer number) {
        baseMapper.subtractStock(goodsId,number);
        return ResultMsg.success();
    }
}
