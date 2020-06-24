package com.dili.trading.service.impl;

import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.order.domain.TransitionDepartureApply;
import com.dili.order.domain.TransitionDepartureSettlement;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.TransitionDepartureApplyRpc;
import com.dili.trading.rpc.TransitionDepartureSettlementRpc;
import com.dili.trading.service.TransitionDepartureSettlementService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TransitionDepartureSettlementServiceImpl implements TransitionDepartureSettlementService {

    @Autowired
    private TransitionDepartureApplyRpc transitionDepartureApplyRpc;

    @Autowired
    private TransitionDepartureSettlementRpc transitionDepartureSettlementRpc;

    /**
     * 根据申请单信息，新增一条结算单信息
     *
     * @param transitionDepartureSettlement
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场申请单审批", operationType = "update", systemCode = "ORDERS")
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
        //设置支付状态为未结算
        transitionDepartureSettlement.setPayStatus(1);
        //根据申请单id拿到申请单，修改申请单的支付状态为1（未结算）
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
        //盘点是否请求成功
        if (oneByID.isSuccess()) {
            //判断是否存在申请单
            if (Objects.nonNull(oneByID.getData())) {
                TransitionDepartureApply data = oneByID.getData();
                data.setPayStatus(1);
                BaseOutput update = transitionDepartureApplyRpc.update(data);
                //判断是否更新失败，如果更新失败，那就抛出异常
                if (!update.isSuccess()) {
                    throw new RuntimeException();
                }
            } else {
                return BaseOutput.failure("操作失败，申请单不存在");
            }
        } else {
            return BaseOutput.failure("操作失败");
        }
        //更新完成之后，插入缴费单信息，必须在这之前发起请求，到支付系统，拿到支付单号
        /**
         * 申请支付单号还没有接入
         */
        BaseOutput<TransitionDepartureSettlement> update = transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
        //更新结算单不成功的时候
        if (!update.isSuccess()) {
            throw new RuntimeException();
        }
        TransitionDepartureSettlement data = update.getData();
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
        LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
        LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        return BaseOutput.success();
    }

    @Override
    public BaseOutput revocator(TransitionDepartureSettlement transitionDepartureSettlement) {
        //判断结算单的支付状态是否为2（已结算）,不是则直接返回
        if (transitionDepartureSettlement.getPayStatus() != 1) {
            return BaseOutput.failure("只有已结算的结算单可以撤销");
        }
        //设置为已撤销的支付状态
        transitionDepartureSettlement.setPayStatus(3);
        //根据结算单的apply_id拿到申请单信息
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
        if (!oneByID.isSuccess()) {
            return BaseOutput.failure(oneByID.getMessage());
        }
        if (Objects.isNull(oneByID.getData())) {
            return BaseOutput.failure("申请单不存在");
        }
        TransitionDepartureApply data = oneByID.getData();
        //设置申请单支付状态为已撤销
        data.setPayStatus(3);
        //先更新申请单，判断是否更新成功，没有更新成功则抛出异常
        BaseOutput update = transitionDepartureApplyRpc.update(data);
        if (!update.isSuccess()) {
            throw new RuntimeException();
        }
        //修改结算单的支付状态
        BaseOutput update1 = transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
        //判断结算单修改是否成功，不成功则抛出异常
        if (!update1.isSuccess()) {
            throw new RuntimeException();
        }
        return BaseOutput.success();
    }

    @Override
    public BaseOutput pay(TransitionDepartureSettlement transitionDepartureSettlement) {
        //判断结算单的支付状态是否为1（未结算）,不是则直接返回
        if (transitionDepartureSettlement.getPayStatus() != 1) {
            return BaseOutput.failure("只有未结算的结算单可以结算");
        }
        //设置为已支付状态
        transitionDepartureSettlement.setPayStatus(2);
        //根据结算单apply_id获取到对应申请单
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
        //判断请求是否成功
        if (!oneByID.isSuccess()) {
            return BaseOutput.failure(oneByID.getMessage());
        }
        //判断申请单是否存在
        if (Objects.isNull(oneByID.getData())) {
            return BaseOutput.failure("申请单不存在");
        }
        //获取到申请单
        TransitionDepartureApply data = oneByID.getData();
        //设置申请单支付状态为已支付
        data.setPayStatus(2);
        BaseOutput update = transitionDepartureApplyRpc.update(data);
        //如果申请单更新失败，则抛出异常
        if (!update.isSuccess()) {
            throw new RuntimeException();
        }
        //修改结算单的支付状态
        BaseOutput update1 = transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
        //如果结算单修改失败，则抛出异常
        if (!update1.isSuccess()) {
            throw new RuntimeException();
        }
        return BaseOutput.success();
    }

}
