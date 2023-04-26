package org.apache.rocketmq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.enums.ShopCode;
import com.example.common.exception.ShopException;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Goods;
import com.example.domain.po.GoodsOrderLog;
import org.apache.rocketmq.dao.GoodsMapper;
import org.apache.rocketmq.service.GoodsOrderLogService;
import org.apache.rocketmq.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 商品表 服务实现类
* @since seeingflow
*/
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private GoodsOrderLogService goodsOrderLogService;

    @Override
    public ResultMsg subtractStock(Long goodsId, Integer number) {
        baseMapper.subtractStock(goodsId,number);
        return ResultMsg.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultMsg reduceGoodsNum(GoodsOrderLog goodsNumberLog) {
        if (goodsNumberLog == null ||
                goodsNumberLog.getGoodsNumber() == null ||
                goodsNumberLog.getOrderId() == null ||
                goodsNumberLog.getGoodsId() == null ||
                goodsNumberLog.getGoodsNumber().intValue() <= 0) {
            ShopException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        Goods goods = baseMapper.selectById(goodsNumberLog.getGoodsId());
        if(goods.getGoodsNumber()<goodsNumberLog.getGoodsNumber()){
            //库存不足
            ShopException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        //减去库存
        baseMapper.subtractStock(goodsNumberLog.getGoodsId(),goodsNumberLog.getGoodsNumber());

        //记录库存操作日志
        goodsOrderLogService.addGoodsOrderLog(goodsNumberLog);
        return ResultMsg.success();
    }

}
