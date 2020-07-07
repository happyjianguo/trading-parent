package com.dili.trading.service;

import com.dili.orders.domain.TransitionDepartureSettlement;
import com.dili.ss.domain.BaseOutput;

/**
 * 结算单service
 */
public interface TransitionDepartureSettlementService {
    /**
     * 新增结算单，新增的时候需要调用支付系统获取支付单，并且更改对应申请单的状态为未结算
     *
     * @param transitionDepartureSettlement
     * @return
     */
    BaseOutput insert(TransitionDepartureSettlement transitionDepartureSettlement);

    /**
     * 撤销结算单，需要调用支付系统退钱，再更改对应申请单的支付状态为已撤销
     *
     * @param transitionDepartureSettlement
     * @return
     */
    BaseOutput revocator(TransitionDepartureSettlement transitionDepartureSettlement);

    /**
     * 缴费，需要调用支付系统收钱，并且更改对应申请单的支付状态为已支付
     *
     * @param transitionDepartureSettlement
     * @return
     */
    BaseOutput pay(TransitionDepartureSettlement transitionDepartureSettlement,String password);
}
