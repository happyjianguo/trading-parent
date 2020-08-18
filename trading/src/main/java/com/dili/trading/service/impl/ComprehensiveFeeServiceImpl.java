package com.dili.trading.service.impl;

import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.orders.domain.ComprehensiveFee;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.ComprehensiveFeeRpc;
import com.dili.trading.service.ComprehensiveFeeService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ComprehensiveFeeServiceImpl implements ComprehensiveFeeService {

    @Autowired
    private ComprehensiveFeeRpc comprehensiveFeeRpc;

    /**
     * 根据申请单信息，新增一条结算单信息
     *
     * @param comprehensiveFee
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "检测收费新增", operationType = "add", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<ComprehensiveFee> insertComprehensiveFee(ComprehensiveFee comprehensiveFee) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置创建时间
        comprehensiveFee.setCreatedTime(LocalDateTime.now());
        //设置结算员id
        comprehensiveFee.setOperatorId(userTicket.getId());
        //设置结算员姓名
        comprehensiveFee.setOperatorName(userTicket.getRealName());
        //结算时间
        comprehensiveFee.setOperatorTime(LocalDateTime.now());
        //设置创建人id
        comprehensiveFee.setCreatorId(userTicket.getId());
        //设置数据创建时间
        comprehensiveFee.setCreatedTime(LocalDateTime.now());
        //将车牌号的小写变为大写
        if (StringUtils.isNotBlank(comprehensiveFee.getPlate())) {
            comprehensiveFee.setPlate(comprehensiveFee.getPlate().toUpperCase());
        }
        BaseOutput<ComprehensiveFee> comprehensiveFeeBaseOutput = comprehensiveFeeRpc.insert(comprehensiveFee);
        if (comprehensiveFeeBaseOutput.isSuccess()) {
            ComprehensiveFee data = comprehensiveFeeBaseOutput.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return comprehensiveFeeBaseOutput;
    }

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "检测收费结算单支付", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput pay(Long id, String password) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<ComprehensiveFee> pay = comprehensiveFeeRpc.pay(id, password, userTicket.getFirmId(), userTicket.getDepartmentId(), userTicket.getUserName(), userTicket.getId(), userTicket.getRealName(), userTicket.getUserName());
        if (pay.isSuccess()) {
            ComprehensiveFee data = pay.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return pay;
    }
}
