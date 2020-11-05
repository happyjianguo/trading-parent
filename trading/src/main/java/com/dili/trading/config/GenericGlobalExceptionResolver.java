package com.dili.trading.config;

import com.alibaba.fastjson.JSON;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;

/**
 * @author: miaoguoxin
 * @date: 2020/4/8 11:22
 */
@ControllerAdvice
public class GenericGlobalExceptionResolver {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericGlobalExceptionResolver.class);

    /**
     * 表单参数校验
     *
     * @author miaoguoxin
     * @date 2020/7/10
     */
    @ExceptionHandler({BindException.class})
    public String handleFormArgException(BindException e) throws IOException {
        return this.writeErrorResp(ResultCode.PARAMS_ERROR, errorMessage(e.getBindingResult()), e);
    }

    /**
     * 方法参数校验处理，配合validator
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public String handleMethodArgException(MethodArgumentNotValidException e) throws IOException {
        return this.writeErrorResp(ResultCode.PARAMS_ERROR, errorMessage(e.getBindingResult()), e);
    }


    /**
     * 单参数校验异常处理
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public String handleConstraintViolationException(ConstraintViolationException e) throws IOException {
        return this.writeErrorResp(ResultCode.PARAMS_ERROR, e.getMessage(), e);
    }

    /**
     * 415处理
     */
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public String handle415Exception(HttpMediaTypeNotSupportedException e) throws IOException {
        return this.writeErrorResp(ResultCode.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", e);
    }

    /**
     * 400处理
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public String handle400Exception(HttpMessageNotReadableException e) throws IOException {
        LOGGER.error("参数有误：{}", e.getMessage());
        return this.writeErrorResp(ResultCode.INVALID_REQUEST, "Bad Request", e);
    }

    /**
     * 405处理
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public String handle405Exception(HttpRequestMethodNotSupportedException e) throws IOException {
        return this.writeErrorResp(ResultCode.METHOD_NOT_ALLOWED, "Method Not Allowed", e);
    }

    /**
     * 业务异常处理
     */
    @ExceptionHandler({BusinessException.class})
    public String handlerBusinessException(BusinessException e) throws IOException {
        return this.writeErrorResp(e.getCode(), e.getMessage(), e);
    }

    /**
     * 处理未自定义的异常
     */
    @ExceptionHandler({Exception.class})
    public String handlerOtherException(Exception e) throws IOException {
        LOGGER.error(e.getMessage(), e);
        return this.writeErrorResp(ResultCode.APP_ERROR, "服务异常，请联系管理员", e);
    }

    private String writeErrorResp(String errorCode, String errorMsg, Throwable e) throws IOException {
        if (request.getRequestURI().contains(".html")) {
            request.setAttribute("exception", e);
            return SpringUtil.getProperty("error.page.default", "error/default");
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(BaseOutput.create(errorCode, errorMsg)));
        return null;
    }

    private static String errorMessage(BindingResult result) {
        if (result == null) {
            return "";
        }
        List<ObjectError> allErrors = result.getAllErrors();
        return allErrors.stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("未知参数错误");
    }

}
