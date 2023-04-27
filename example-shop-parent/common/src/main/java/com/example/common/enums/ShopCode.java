package com.example.common.enums;

import com.example.common.exception.IErrorCode;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/26 16:56
 * @Desc
 * @since seeingflow
 */
public enum ShopCode implements IErrorCode {

//    异常枚举
    SHOP_ORDER_INVALID(818101001, "订单不存在"),
    SHOP_GOODS_NO_EXIST(818101002, "商品不存在"),
    SHOP_USER_NO_EXIST(818101003, "用户不存在"),
    SHOP_GOODS_PRICE_INVALID(818101004, "单价不合法"),
    SHOP_GOODS_NUM_NOT_ENOUGH(818101005, "库存不足"),
    SHOP_ORDERAMOUNT_INVALID(818101006, "订单价格不正确"),
    SHOP_MONEY_PAID_LESS_ZERO(818101007, "支付余额不能小于0"),
    SHOP_MONEY_PAID_INVALID(818101008, "用户余额不足"),
    SHOP_REDUCE_GOODS_NUM_FAIL(818101009, "扣减库存失败"),
    SHOP_REQUEST_PARAMETER_VALID(818101010, "参数为空"),
    SHOP_COUPON_USE_FAIL(818101011, "优惠券使用失败"),
    SHOP_ORDER_PAY_STATUS_IS_PAY(2, "订单已付款"),
    SHOP_ORDER_PAY_STATUS_NO_PAY(0, "未付款，暂无退款信息"),
    SHOP_USER_MONEY_REFUND_ALREADY(818101014, "已退款，请勿重复操作"),

    SHOP_USER_MONEY_REDUCE_FAIL(818101015, "扣减用户余额失败"),

    SHOP_ORDER_CONFIRM_FAIL(818101016, "订单确认失败"),
    SHOP_MQ_SEND_MESSAGE_FAIL(818101017, "MQ消息发送失败"),
    SHOP_MQ_TOPIC_IS_EMPTY(818101018, "MQ-topic不能为空"),
    SHOP_MQ_MESSAGE_BODY_IS_EMPTY(818101019, "MQ-消息内容不能为空"),

//    订单枚举
    SHOP_ORDER_NO_CONFIRM(0, "未确认"),
    SHOP_ORDER_CONFIRM(1, "已确认"),
    SHOP_ORDER_CANCEL(2,"已取消"),

//    优惠券枚举
    SHOP_COUPON_UNUSED(0, "未使用"),
    SHOP_COUPON_ISUSED(1, "已使用"),

//    金额日志类型
    SHOP_USER_MONEY_PAID(1, "订单付款"),
    SHOP_USER_MONEY_REFUND(2,"订单退款"),

//消息处理状态
    SHOP_MQ_MESSAGE_STATUS_PROCESSING(0,"消息正在处理"),
    SHOP_MQ_MESSAGE_STATUS_SUCCESS(1,"消息处理成功"),
    SHOP_MQ_MESSAGE_STATUS_FAIL(2,"消息处理失败"),

        ;

    private int code;
    private String message;

    ShopCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
