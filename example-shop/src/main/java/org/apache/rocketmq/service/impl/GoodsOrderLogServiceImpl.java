package org.apache.rocketmq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.rocketmq.dao.GoodsOrderLogMapper;
import org.apache.rocketmq.model.GoodsOrderLog;
import org.apache.rocketmq.service.GoodsOrderLogService;
import org.apache.rocketmq.utils.ResultMsg;
import org.springframework.stereotype.Service;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单商品日志表 服务实现类
* @since seeingflow
*/
@Service
public class GoodsOrderLogServiceImpl extends ServiceImpl<GoodsOrderLogMapper, GoodsOrderLog> implements GoodsOrderLogService {

    @Override
    public ResultMsg addGoodsOrderLog(GoodsOrderLog goodsOrderLog) {
        baseMapper.insert(goodsOrderLog);
        return ResultMsg.success();
    }
}
