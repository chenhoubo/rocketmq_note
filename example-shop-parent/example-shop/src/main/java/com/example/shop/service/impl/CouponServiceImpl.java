package com.example.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.enums.ShopCode;
import com.example.common.exception.ShopException;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Coupon;
import com.example.shop.dao.CouponMapper;
import com.example.shop.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 优惠券表 服务实现类
* @since seeingflow
*/
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultMsg changeCouponStatus(Coupon coupon) {
        //判断请求参数是否合法
        if (coupon == null || Objects.nonNull(coupon.getCouponId())) {
            ShopException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        //更新优惠券状态为已使用
        baseMapper.updateById(coupon);
        return ResultMsg.success();
    }
}
