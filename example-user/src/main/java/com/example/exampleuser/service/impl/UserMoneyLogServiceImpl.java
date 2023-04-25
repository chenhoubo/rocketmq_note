package com.example.exampleuser.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.exampleuser.dao.UserMoneyLogMapper;
import com.example.exampleuser.model.UserMoneyLog;
import com.example.exampleuser.service.UserMoneyLogService;
import com.example.exampleuser.utils.ResultMsg;
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
