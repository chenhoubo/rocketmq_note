package org.apache.rocketmq.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.service.GoodsService;
import org.apache.rocketmq.utils.ResultMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 商品表 前端控制器
* @since seeingflow
*/
@Slf4j
@Api(value = "GoodsController",tags = "商品表")
@RestController
@RequestMapping("/goods")
    public class GoodsController {

    @Autowired
    public GoodsService goodsService;


    @ApiOperation("扣减库存")
    @GetMapping("/subtractStock")
    public ResultMsg subtractStock(@RequestParam("goodsId") Long goodsId,
                                   @RequestParam("number") Integer number){
        return goodsService.subtractStock(goodsId, number);
    }

}
