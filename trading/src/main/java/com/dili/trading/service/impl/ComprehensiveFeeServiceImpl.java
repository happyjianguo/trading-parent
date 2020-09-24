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

/**
 * 检测收费服务实现类
 *
 *@author  Henry.Huang
 *@date  2020/08/20
 *
 */
@Service
public class ComprehensiveFeeServiceImpl implements ComprehensiveFeeService {

    @Autowired
    private ComprehensiveFeeRpc comprehensiveFeeRpc;

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
        //设置数据修改人ID
        comprehensiveFee.setModifierId(userTicket.getId());
        //设置数据修改时间
        comprehensiveFee.setModifiedTime(LocalDateTime.now());
        //设置检测收费单归属部门
        comprehensiveFee.setDepartmentId(userTicket.getDepartmentId());
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
        BaseOutput<ComprehensiveFee> pay = comprehensiveFeeRpc.pay(id, password, userTicket.getFirmId(), userTicket.getId(), userTicket.getRealName(), userTicket.getUserName());
        if (pay.isSuccess()) {
            ComprehensiveFee data = pay.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return pay;
    }

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "检测收费结算单撤销", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<ComprehensiveFee> revocator(ComprehensiveFee comprehensiveFee, String operatorPassword) {
        //获取当前登录用户
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //修改结算单的支付状态
        //设置撤销人员相关信息
        LocalDateTime now = LocalDateTime.now();
        comprehensiveFee.setRevocatorId(userTicket.getId());
        comprehensiveFee.setRevocatorName(userTicket.getRealName());
        comprehensiveFee.setRevocatorTime(now);
        BaseOutput<ComprehensiveFee> revocator = this.comprehensiveFeeRpc.revocator(comprehensiveFee, userTicket.getRealName(),userTicket.getId(), operatorPassword, userTicket.getUserName());
        if (revocator.isSuccess()) {
            ComprehensiveFee data = revocator.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return revocator;
    }
}
