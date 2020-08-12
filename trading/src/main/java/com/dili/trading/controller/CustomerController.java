package com.dili.trading.controller;

import com.dili.customer.sdk.domain.Customer;
import com.dili.customer.sdk.domain.dto.CustomerQueryInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.dto.UserAccountCardResponseDto;
import com.dili.orders.rpc.AccountRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 模糊查询客户
 */
@RequestMapping("/customer")
@Controller
public class CustomerController {

    @Autowired
    private CustomerRpc customerRpc;


    @Autowired
    private AccountRpc accountRpc;

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

    /**
     * 根据卡号查询客户信息
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/listAccount.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput listAccount(String cardNo) {
        BaseOutput<UserAccountCardResponseDto> oneAccountCard = accountRpc.getOneAccountCard(cardNo);
        if (!oneAccountCard.isSuccess()) {
            return oneAccountCard;
        }
        return customerRpc.get(oneAccountCard.getData().getCustomerId(), oneAccountCard.getData().getFirmId());
    }

}
