package com.dili.trading.service.impl;

import com.dili.order.domain.TransitionDepartureApply;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.TransitionDepartureApplyRpc;
import com.dili.trading.rpc.UidRpc;
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

    @Autowired
    private UidRpc uidRpc;

    @Override
    public BaseOutput insert(TransitionDepartureApply transitionDepartureApply) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置code编号
        transitionDepartureApply.setCode(uidRpc.getFirmCode().getData());
        //设置申请时间
        transitionDepartureApply.setOriginatorTime(LocalDateTime.now());
        //设置申请员工id
        transitionDepartureApply.setOriginatorId(userTicket.getId());
        //设置申请员工用户名
        transitionDepartureApply.setOriginatorName(userTicket.getRealName());
        //设置申请员工的工号即为登录用户名
        transitionDepartureApply.setOriginatorCode(userTicket.getUserName());
        return transitionDepartureApplyRpc.insert(transitionDepartureApply);
    }

    @Override
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
        return transitionDepartureApplyRpc.update(transitionDepartureApply);
    }

}
