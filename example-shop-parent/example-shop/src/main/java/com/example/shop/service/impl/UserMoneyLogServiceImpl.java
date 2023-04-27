package com.example.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.UserMoneyLog;
import com.example.shop.dao.UserMoneyLogMapper;
import com.example.shop.service.UserMoneyLogService;
import org.springframework.stereotype.Service;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户余额日志表 服务实现类
* @since seeingflow
*/
@Service
public class UserMoneyLogServiceImpl extends ServiceImpl<UserMoneyLogMapper, UserMoneyLog> implements UserMoneyLogService {

    @Override
    public ResultMsg addUserMoneyLog(UserMoneyLog userMoneyLog) {
        baseMapper.insert(userMoneyLog);
        return ResultMsg.success();
    }
}
