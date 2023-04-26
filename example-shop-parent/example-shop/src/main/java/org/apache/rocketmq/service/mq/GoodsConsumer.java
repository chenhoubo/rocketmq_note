package org.apache.rocketmq.service.mq;

import com.alibaba.fastjson.JSONObject;
import com.example.domain.po.GoodsOrderLog;
import org.apache.rocketmq.service.GoodsService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/25 16:45
 * @Desc
 * @since seeingflow
 */
@Service
@RocketMQMessageListener(topic = "goodsTopic", consumerGroup = "shopGroup", messageModel = MessageModel.CLUSTERING)
public class GoodsConsumer implements RocketMQListener<String> {
    @Autowired
    private GoodsService goodsService;

    @Override
    public void onMessage(String msg) {
        GoodsOrderLog goodsOrderLog = JSONObject.parseObject(msg, GoodsOrderLog.class);
        //扣减库存
        goodsService.subtractStock(goodsOrderLog.getGoodsId(),goodsOrderLog.getGoodsNumber());
    }
}
