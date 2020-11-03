package com.dili.trading.service.impl;

import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.orders.constants.OrdersConstant;
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
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<ComprehensiveFee> insertComprehensiveFee(ComprehensiveFee comprehensiveFee) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        setComprehensiveFeeValue(comprehensiveFee, userTicket);
        BaseOutput<ComprehensiveFee> comprehensiveFeeBaseOutput = comprehensiveFeeRpc.insert(comprehensiveFee);
        return comprehensiveFeeBaseOutput;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<ComprehensiveFee> pay(Long id, String password) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<ComprehensiveFee> pay = comprehensiveFeeRpc.pay(id, password, userTicket.getFirmId(), userTicket.getId(), userTicket.getRealName(), userTicket.getUserName());
        return pay;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<ComprehensiveFee> revocator(ComprehensiveFee comprehensiveFee, String operatorPassword) {
        //获取当前登录用户
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置撤销人员相关信息
        LocalDateTime now = LocalDateTime.now();
        comprehensiveFee.setRevocatorId(userTicket.getId());
        comprehensiveFee.setRevocatorName(userTicket.getRealName());
        comprehensiveFee.setRevocatorTime(now);
        BaseOutput<ComprehensiveFee> revocator = this.comprehensiveFeeRpc.revocator(comprehensiveFee, userTicket.getRealName(),userTicket.getId(), operatorPassword, userTicket.getUserName());
        return revocator;
    }

    /**
     * 设置综合收费单据值
     * @param comprehensiveFee 综合收费单据
     * @param userTicket 登录人信息
     */
    private void setComprehensiveFeeValue(ComprehensiveFee comprehensiveFee, UserTicket userTicket) {
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
    }
}
