package com.example.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Coupon;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 优惠券表 服务类
* @since seeingflow
*/
public interface CouponService extends IService<Coupon> {

    ResultMsg changeCouponStatus(Coupon coupon);

}

