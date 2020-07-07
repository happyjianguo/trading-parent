package com.dili.trading.service;

import com.dili.orders.domain.TransitionDepartureApply;
import com.dili.ss.domain.BaseOutput;

/**
 * 申请单service
 */
public interface TransitionDepartureApplyService {
    /**
     * 插入一条新的申请单
     *
     * @param transitionDepartureApply
     * @return
     */
    BaseOutput insert(TransitionDepartureApply transitionDepartureApply);

    /**
     * 对申请单做审批
     *
     * @param transitionDepartureApply
     * @return
     */
    BaseOutput approval(TransitionDepartureApply transitionDepartureApply);
}
