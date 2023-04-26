package org.apache.rocketmq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.UserMoneyLog;
import org.apache.ibatis.annotations.Mapper;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户余额日志表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface UserMoneyLogMapper extends BaseMapper<UserMoneyLog> {

}
