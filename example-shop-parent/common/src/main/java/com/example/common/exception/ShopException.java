package com.example.common.exception;

import com.example.common.enums.ShopCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

@Data
@Slf4j
public class ShopException extends RuntimeException {

    private int code;
    private String message;
    private Object data;

    public ShopException(IErrorCode iErrorCode) {
        super();
        this.code = iErrorCode.getCode();
        this.message = iErrorCode.getMessage();
    }

    public ShopException(IErrorCode iErrorCode, Object... params) {
        this.code = iErrorCode.getCode();
        this.message = doFormat(iErrorCode.getCode(),iErrorCode.getMessage(),params);
    }

    public ShopException(int code, @Nullable String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public ShopException(IErrorCode iErrorCode, Object data) {
        super();
        this.code = iErrorCode.getCode();
        this.message = iErrorCode.getMessage();
        this.data = data;
    }

    public static void cast(ShopCode shopOrderInvalid) {
        throw new ShopException(shopOrderInvalid);
    }


    @Override
    public String getMessage() {
        return message;
    }

    public static String doFormat(int code, String messagePattern, Object... params) {
        StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);
        int i = 0;
        int j;
        int l;
        for (l = 0; l < params.length; l++) {
            j = messagePattern.indexOf("{}", i);
            if (j == -1) {
                log.error("[doFormat][参数过多：错误码({})|错误内容({})|参数({})", code, messagePattern, params);
                if (i == 0) {
                    return messagePattern;
                } else {
                    sbuf.append(messagePattern.substring(i));
                    return sbuf.toString();
                }
            } else {
                sbuf.append(messagePattern, i, j);
                sbuf.append(params[l]);
                i = j + 2;
            }
        }
        if (messagePattern.indexOf("{}", i) != -1) {
            log.error("[doFormat][参数过少：错误码({})|错误内容({})|参数({})", code, messagePattern, params);
        }
        sbuf.append(messagePattern.substring(i));
        return sbuf.toString();
    }
}
