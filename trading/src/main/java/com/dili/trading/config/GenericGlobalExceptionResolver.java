package com.dili.trading.config;

import com.dili.ss.domain.BaseOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.SocketTimeoutException;

/**
 * @author:
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
     * 连接超时捕获
     *
     * @author
     * @date 2020/7/10
     */
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public BaseOutput timeOut(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return BaseOutput.failure("服务异常，请联系管理员");
    }


}
