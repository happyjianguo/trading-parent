package com.dili.trading.controller;


import com.dili.order.domain.TransitionDepartureApply;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.OrderServiceRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * TransitionDepartureApplyController
 */
@Controller
@RequestMapping("/transitionDepartureApplyController")
public class TransitionDepartureApplyController {
    @Autowired
    private OrderServiceRpc assetsRpc;

    /**
     * getOneByCustomerID
     * @param transitionDepartureApply
     * @return
     */
    @RequestMapping(value = "/getOneByCustomerID", method = {RequestMethod.GET, RequestMethod.POST})
    BaseOutput<TransitionDepartureApply> getOneByCustomerID(@RequestBody TransitionDepartureApply transitionDepartureApply) {
        return assetsRpc.getOneByCustomerID(transitionDepartureApply);
    }
}
