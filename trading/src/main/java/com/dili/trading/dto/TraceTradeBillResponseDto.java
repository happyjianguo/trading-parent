package com.dili.trading.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Auther: miaoguoxin
 * @Date: 2020/11/6 15:01
 * @Description:
 */
public class TraceTradeBillResponseDto implements Serializable {
    /**订单id*/
    private Long build;
    /**检测记录时间*/
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestDetectTime;
    /**检测人员*/
    private String latestDetectOperator;
    /**检测数值*/
    private String latestPdResult;
    /**检测结果描述*/
    private String detectStateDesc;

    public String getDetectStateDesc() {
        return detectStateDesc;
    }

    public void setDetectStateDesc(String detectStateDesc) {
        this.detectStateDesc = detectStateDesc;
    }

    public Long getBuild() {
        return build;
    }

    public void setBuild(Long build) {
        this.build = build;
    }

    public LocalDateTime getLatestDetectTime() {
        return latestDetectTime;
    }

    public void setLatestDetectTime(LocalDateTime latestDetectTime) {
        this.latestDetectTime = latestDetectTime;
    }

    public String getLatestDetectOperator() {
        return latestDetectOperator;
    }

    public void setLatestDetectOperator(String latestDetectOperator) {
        this.latestDetectOperator = latestDetectOperator;
    }

    public String getLatestPdResult() {
        return latestPdResult;
    }

    public void setLatestPdResult(String latestPdResult) {
        this.latestPdResult = latestPdResult;
    }
}
