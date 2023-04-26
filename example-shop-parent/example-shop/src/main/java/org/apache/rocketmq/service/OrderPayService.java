package org.apache.rocketmq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.OrderPay;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单支付表 服务类
* @since seeingflow
*/
public interface OrderPayService extends IService<OrderPay> {

    ResultMsg addOrderPay(OrderPay orderPay);

    ResultMsg updateIsPaid( Long payId, Integer isPaid);
}
