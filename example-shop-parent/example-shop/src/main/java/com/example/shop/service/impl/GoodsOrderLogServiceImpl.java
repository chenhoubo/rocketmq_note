package com.example.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.GoodsOrderLog;
import com.example.shop.dao.GoodsOrderLogMapper;
import com.example.shop.service.GoodsOrderLogService;
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
