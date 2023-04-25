package com.example.exampleuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.exampleuser.model.UserMoneyLog;
import com.example.exampleuser.utils.ResultMsg;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户余额日志表 服务类
* @since seeingflow
*/
public interface UserMoneyLogService extends IService<UserMoneyLog> {

    ResultMsg addUserMoneyLog(UserMoneyLog userMoneyLog);
}
