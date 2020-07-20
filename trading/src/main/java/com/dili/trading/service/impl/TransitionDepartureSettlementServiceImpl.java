package com.dili.trading.service.impl;

import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.orders.domain.TransitionDepartureSettlement;
import com.dili.orders.rpc.AccountRpc;
import com.dili.orders.rpc.JmsfRpc;
import com.dili.orders.rpc.PayRpc;
import com.dili.orders.rpc.UidRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.TransitionDepartureApplyRpc;
import com.dili.trading.rpc.TransitionDepartureSettlementRpc;
import com.dili.trading.service.TransitionDepartureSettlementService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransitionDepartureSettlementServiceImpl implements TransitionDepartureSettlementService {

    @Autowired
    private TransitionDepartureApplyRpc transitionDepartureApplyRpc;

    @Autowired
    private TransitionDepartureSettlementRpc transitionDepartureSettlementRpc;

    @Autowired
    private UidRpc uidRpc;

    @Autowired
    private JmsfRpc jmsfRpc;

    @Autowired
    private AccountRpc accountRpc;

    @Autowired
    private PayRpc payRpc;

    /**
     * 根据申请单信息，新增一条结算单信息
     *
     * @param transitionDepartureSettlement
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场结算单新增", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput insert(TransitionDepartureSettlement transitionDepartureSettlement) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置创建时间
        transitionDepartureSettlement.setCreateTime(LocalDateTime.now());
        //设置操作员id
        transitionDepartureSettlement.setOperatorId(userTicket.getId());
        //设置操作员姓名
        transitionDepartureSettlement.setOperatorName(userTicket.getRealName());
        //设置操作员登录用户名
        transitionDepartureSettlement.setOperatorCode(userTicket.getUserName());
        BaseOutput<TransitionDepartureSettlement> transitionDepartureSettlementBaseOutput = transitionDepartureSettlementRpc.insertTransitionDepartureSettlement(transitionDepartureSettlement);
        if (transitionDepartureSettlementBaseOutput.isSuccess()) {
            TransitionDepartureSettlement data = transitionDepartureSettlementBaseOutput.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return transitionDepartureSettlementBaseOutput;
    }

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场结算单撤销", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput revocator(TransitionDepartureSettlement transitionDepartureSettlement) {
        //获取当前登录用户
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //修改结算单的支付状态
        //设置撤销人员相关信息
        transitionDepartureSettlement.setRevocatorId(userTicket.getId());
        transitionDepartureSettlement.setRevocatorName(userTicket.getRealName());
        transitionDepartureSettlement.setRevocatorTime(LocalDateTime.now());
        BaseOutput<TransitionDepartureSettlement> revocator = transitionDepartureSettlementRpc.revocator(transitionDepartureSettlement);
        if (revocator.isSuccess()) {
            TransitionDepartureSettlement data = revocator.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return revocator;
    }

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场结算单支付", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput pay(Long id, String password) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<TransitionDepartureSettlement> pay = transitionDepartureSettlementRpc.pay(id, password, userTicket.getFirmId(), userTicket.getDepartmentId(), userTicket.getUserName(), userTicket.getId(), userTicket.getRealName(), userTicket.getUserName());
        if (pay.isSuccess()) {
            TransitionDepartureSettlement data = pay.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return pay;
    }

}
