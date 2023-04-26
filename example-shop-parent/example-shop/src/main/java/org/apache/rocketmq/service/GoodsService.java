package org.apache.rocketmq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Goods;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 商品表 服务类
* @since seeingflow
*/
public interface GoodsService extends IService<Goods> {
    ResultMsg subtractStock(Long goodsId, Integer number);
}
