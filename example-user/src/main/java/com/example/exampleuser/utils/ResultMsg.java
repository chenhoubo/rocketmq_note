package com.example.exampleuser.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/25 16:37
 * @Desc
 * @since seeingflow
 */
@Data
@ApiModel(value = "ResultMsg", description = "通用响应结果")
public class ResultMsg<T> {

    public ResultMsg() {
        this.data = null;
        this.code = 0;
        this.msg = "";
        this.errorMsg = "";
    }

    public ResultMsg(T data) {
        this.data = data;
        this.code = 0;
        this.msg = "";
        this.errorMsg = "";
    }

    public ResultMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultMsg(int code, String msg, String errorMsg) {
        this.code = code;
        this.msg = msg;
        this.errorMsg = errorMsg;
    }

    public ResultMsg(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    @ApiModelProperty(value = "错误码")
    private int code;

    @ApiModelProperty(value = "提示信息")
    private String msg;

    @ApiModelProperty(value = "异常信息")
    private String errorMsg;

    @ApiModelProperty(value = "响应结果")
    private T data;

    public static <T> ResultMsg<T> success() {
        return new ResultMsg<T>(200, "成功");
    }

    public static <T> ResultMsg<T> success(T data) {
        return new ResultMsg<T>(200, "成功", data);
    }

    public static <T> ResultMsg<T> error() {
        return new ResultMsg<T>(500, "失败");
    }

    public static <T> ResultMsg<T> error(String errorMsg) {
        return new ResultMsg<T>(500, errorMsg);
    }

}
