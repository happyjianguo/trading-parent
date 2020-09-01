package com.dili.trading.service.impl;

import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.orders.domain.TransitionDepartureApply;
import com.dili.orders.rpc.UidRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.TransitionDepartureApplyRpc;
import com.dili.trading.service.TransitionDepartureApplyService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransitionDepartureApplyServiceImpl implements TransitionDepartureApplyService {

    @Autowired
    private TransitionDepartureApplyRpc transitionDepartureApplyRpc;

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "新增转离场申请单", operationType = "add", systemCode = "ORDERS")
    public BaseOutput insert(TransitionDepartureApply transitionDepartureApply) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置code编号
//        transitionDepartureApply.setCode(uidRpc.bizNumber("sg_zlc_apply").getData());
        //设置申请时间
        transitionDepartureApply.setOriginatorTime(LocalDateTime.now());
        //设置申请员工id
        transitionDepartureApply.setOriginatorId(userTicket.getId());
        //设置申请员工用户名
        transitionDepartureApply.setOriginatorName(userTicket.getRealName());
        //设置申请员工的工号即为登录用户名

        transitionDepartureApply.setOriginatorCode(userTicket.getUserName());
        BaseOutput<TransitionDepartureApply> insert = transitionDepartureApplyRpc.insert(transitionDepartureApply);
        if (insert.isSuccess()) {
            TransitionDepartureApply data = insert.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return insert;
    }

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场申请单审批", operationType = "update", systemCode = "ORDERS")
    public BaseOutput approval(TransitionDepartureApply transitionDepartureApply) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置审批时间
        transitionDepartureApply.setApprovalTime(LocalDateTime.now());
        //设置审批人id
        transitionDepartureApply.setApprovalId(userTicket.getId());
        //设置审批人用户名
        transitionDepartureApply.setApprovalName(userTicket.getRealName());
        //设置审批人工号
        transitionDepartureApply.setApprovalCode(userTicket.getUserName());
        BaseOutput update = transitionDepartureApplyRpc.update(transitionDepartureApply);
        if (update.isSuccess()) {
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, transitionDepartureApply.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, transitionDepartureApply.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return update;
    }

}
