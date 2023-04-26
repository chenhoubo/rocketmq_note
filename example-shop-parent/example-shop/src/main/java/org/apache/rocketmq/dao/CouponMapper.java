package org.apache.rocketmq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.Coupon;
import org.apache.ibatis.annotations.Mapper;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 优惠券表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

}
