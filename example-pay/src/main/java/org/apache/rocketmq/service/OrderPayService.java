package org.apache.rocketmq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.rocketmq.model.OrderPay;
import org.apache.rocketmq.utils.ResultMsg;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单支付表 服务类
* @since seeingflow
*/
public interface OrderPayService extends IService<OrderPay> {

    ResultMsg addOrderPay(OrderPay orderPay);
}
