package org.apache.rocketmq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Goods;
import org.apache.rocketmq.dao.GoodsMapper;
import org.apache.rocketmq.service.GoodsService;
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
