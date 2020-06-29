package com.dili.trading.controller;

import com.dili.customer.sdk.domain.dto.CustomerQueryInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 模糊查询客户
 */
@RequestMapping("/customer")
@Controller
public class CustomerController {

    @Autowired
    private CustomerRpc customerRpc;

    /**
     * 客户查询
     *
     * @return
     */
    @RequestMapping(value = "/listNormal.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput listNormal(String likeName) {
        CustomerQueryInput customerQueryInput = new CustomerQueryInput();
        customerQueryInput.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        customerQueryInput.setKeyword(likeName);
        return customerRpc.list(customerQueryInput);
    }
}
