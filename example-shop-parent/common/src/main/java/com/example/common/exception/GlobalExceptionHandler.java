package com.example.common.exception;

import com.example.common.utils.ResultMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Objects;

/**
 * 全局异常处理器
 *
 * @author xinchuang
 * @version v1.0
 * @date 2022/1/24 13:44
 * @since @Copyright(c) seeingflow
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.application.name}")
    private String serverName;

    /**
     * @param e                  :
     * @param httpServletRequest :
     * @return : java.lang.Object
     * @description 接口入参校验
     * @author xinchuang
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object paramException(MethodArgumentNotValidException e, HttpServletRequest httpServletRequest) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (Objects.nonNull(fieldError)) {
            logError(e, httpServletRequest);
            return ResultMsg.error( e.getBindingResult().getFieldError().getDefaultMessage());
        }
        logError(e, httpServletRequest);
        return ResultMsg.error(e.getMessage());
    }

    /**
     * @param e                  :
     * @param httpServletRequest :
     * @return : java.lang.Object
     * @description 全局异常, 自定义异常
     * @author xinchuang
     */
    @ExceptionHandler(Exception.class)
    public Object exceptionHandler(Exception e, HttpServletRequest httpServletRequest) {
        logError(e, httpServletRequest);
        if (e instanceof ShopException) {
            ShopException shopException = (ShopException) e;
            return ResultMsg.error(shopException.getCode(), shopException.getMessage());
        }
        return ResultMsg.error(500, e.getMessage());
    }

    private void logError(Exception ex, HttpServletRequest request) {
        log.error("************************异常开始*******************************");
        log.error(String.valueOf(ex));
        log.error("请求地址：" + request.getRequestURL());
        Enumeration enumeration = request.getParameterNames();
        log.error("请求参数");
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement().toString();
            log.error(name + "---" + request.getParameter(name));
        }
        StackTraceElement[] error = ex.getStackTrace();
        for (StackTraceElement stackTraceElement : error) {
            log.error(stackTraceElement.toString());
        }
        log.error("************************异常结束*******************************");
    }

}
