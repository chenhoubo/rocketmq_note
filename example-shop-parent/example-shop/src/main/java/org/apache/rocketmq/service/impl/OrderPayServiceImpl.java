package org.apache.rocketmq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.OrderPay;
import org.apache.rocketmq.dao.OrderPayMapper;
import org.apache.rocketmq.service.OrderPayService;
import org.springframework.stereotype.Service;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单支付表 服务实现类
* @since seeingflow
*/
@Service
public class OrderPayServiceImpl extends ServiceImpl<OrderPayMapper, OrderPay> implements OrderPayService {

    @Override
    public ResultMsg addOrderPay(OrderPay orderPay) {
        baseMapper.insert(orderPay);
        return ResultMsg.success();
    }

    @Override
    public ResultMsg updateIsPaid(Long payId, Integer isPaid) {
        OrderPay orderPay = new OrderPay();
        orderPay.setPayId(payId);
        orderPay.setIsPaid(isPaid);
        baseMapper.updateById(orderPay);
        return ResultMsg.success();
    }
}
