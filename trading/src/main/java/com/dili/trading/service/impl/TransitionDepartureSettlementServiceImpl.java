package com.dili.trading.service.impl;

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

    @Override
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

        try {
            //根据申请单id拿到申请单，修改申请单的支付状态为1（未结算）
            BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
            if (oneByID.isSuccess()) {
                if (Objects.nonNull(oneByID.getData())) {
                    TransitionDepartureApply data = oneByID.getData();
                    data.setPayStatus(1);
                    transitionDepartureApplyRpc.update(data);
                }
            }
            //更新完成之后，插入缴费单信息，必须在这之前发起请求，到支付系统，拿到支付单号
            /**
             * 申请单号还没有接入
             */
            transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
            return BaseOutput.success();
        } catch (Exception e) {
            e.printStackTrace();
            return BaseOutput.failure("操作失败" + e.getMessage());
        }
    }

    @Override
    public BaseOutput revocator(TransitionDepartureSettlement transitionDepartureSettlement) {
        //设置为已撤销的支付状态
        transitionDepartureSettlement.setPayStatus(3);
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
        try {
            transitionDepartureApplyRpc.update(data);
            //修改结算单的支付状态
            transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
            return BaseOutput.success();
        } catch (Exception e) {
            e.printStackTrace();
            return BaseOutput.failure("撤销失败" + e.getMessage());
        }
    }

    @Override
    public BaseOutput pay(TransitionDepartureSettlement transitionDepartureSettlement) {
        //设置为已支付状态
        transitionDepartureSettlement.setPayStatus(2);
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
        if (!oneByID.isSuccess()) {
            return BaseOutput.failure(oneByID.getMessage());
        }
        if (Objects.isNull(oneByID.getData())) {
            return BaseOutput.failure("申请单不存在");
        }
        TransitionDepartureApply data = oneByID.getData();
        //设置申请单支付状态为已支付
        data.setPayStatus(2);
        try {
            transitionDepartureApplyRpc.update(data);
            //修改结算单的支付状态
            transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
            return BaseOutput.success();
        } catch (Exception e) {
            e.printStackTrace();
            return BaseOutput.failure("缴费失败" + e.getMessage());
        }
    }

}
