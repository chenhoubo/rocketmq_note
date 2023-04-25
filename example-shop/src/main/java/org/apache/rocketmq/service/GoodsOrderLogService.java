package org.apache.rocketmq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.rocketmq.model.GoodsOrderLog;
import org.apache.rocketmq.utils.ResultMsg;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单商品日志表 服务类
* @since seeingflow
*/
public interface GoodsOrderLogService extends IService<GoodsOrderLog> {

    ResultMsg addGoodsOrderLog(GoodsOrderLog goodsOrderLog);

}
