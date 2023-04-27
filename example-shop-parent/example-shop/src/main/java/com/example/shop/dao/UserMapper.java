package com.example.shop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    //扣减用户余额
    void subUserMoney(@Param("userId") Long userId, @Param("userMoney") BigDecimal userMoney);

    void addUserScore(@Param("userId") Long userId, @Param("userScore") Integer userScore);

    void addUserMoney(@Param("userId") Long userId, @Param("userMoney") BigDecimal userMoney);

}
