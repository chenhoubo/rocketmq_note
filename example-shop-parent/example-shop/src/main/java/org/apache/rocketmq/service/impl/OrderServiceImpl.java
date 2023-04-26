package org.apache.rocketmq.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.HttpUtil;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.dao.OrderMapper;
import org.apache.rocketmq.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.Map;

import static com.alibaba.fastjson.JSON.toJSONString;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单表 服务实现类
* @since seeingflow
*/
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public ResultMsg<Long> addOrder(Order order) {
        //1.校验订单

        //2.生成预订单

        try {
            //3.扣减库存

            //4.扣减优惠券

            //5.使用余额

            //6.确认订单

            //7.返回成功状态

        } catch (Exception e) {
            //1.确认订单失败,发送消息

            //2.返回失败状态
        }





        //查询当前用户余额是否足够
        MultiValueMap<String, String> paramMap = (MultiValueMap<String, String>) JSON.parseObject("{\"userId\": \" " + order.getUserId() + "\"}", Map.class);
        String result= HttpUtil.get("http://localhost:8183/user/getUserMoney", paramMap);
        ResultMsg<BigDecimal> resultMsg = JSON.parseObject(toJSONString(result), ResultMsg.class);
        if(!resultMsg.checkSuccess()){
            return ResultMsg.error(resultMsg.getErrorMsg());
        }else {
            //计算商品总价格
            BigDecimal multiply = order.getGoodsPrice().multiply(BigDecimal.valueOf(order.getGoodsNumber()));
            //计算商品价格是否一致
            if(multiply.compareTo(order.getGoodsAmount()) == 0){
                //计算用户余额是否足够
                if(resultMsg.getData().compareTo(order.getPayAmount()) > 0){
                    baseMapper.insert(order);
                }else{
                    return ResultMsg.error("用户余额不足");
                }
                //todo 优惠券是否有效。优惠金额和支付金额是否匹配.....
            }else{
                return ResultMsg.error("商品价格金额不匹配");
            }
        }
        return ResultMsg.success(order.getOrderId());
    }


}
