package com.example.shop.controller;

import com.example.shop.service.GoodsOrderLogService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单商品日志表 前端控制器
* @since seeingflow
*/
@Slf4j
@Api(value = "GoodsOrderLogController",tags = "订单商品日志表")
@RestController
@RequestMapping("/goodsOrderLog")
    public class GoodsOrderLogController {

    @Autowired
    public GoodsOrderLogService goodsOrderLogService;



}
