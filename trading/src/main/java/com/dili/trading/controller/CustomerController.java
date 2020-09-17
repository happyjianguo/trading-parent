package com.dili.trading.controller;

import com.dili.customer.sdk.domain.Customer;
import com.dili.customer.sdk.domain.dto.CustomerQueryInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.constants.TradingConstans;
import com.dili.orders.dto.AccountSimpleResponseDto;
import com.dili.orders.dto.CardQueryDto;
import com.dili.orders.dto.UserAccountCardResponseDto;
import com.dili.orders.rpc.AccountRpc;
import com.dili.orders.rpc.CardRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private FirmRpc firmRpc;

    @Autowired
    private CardRpc cardRpc;

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
        CardQueryDto cardQueryDto = new CardQueryDto();
        cardQueryDto.setCardNo(cardNo);
        BaseOutput<UserAccountCardResponseDto> oneAccountCard = accountRpc.getSingle(cardQueryDto);
        if (!oneAccountCard.isSuccess()) {
            return oneAccountCard;
        }
        return customerRpc.get(oneAccountCard.getData().getCustomerId(), oneAccountCard.getData().getFirmId());
    }

    /**
     * 根据卡号获取客户
     *
     * @param cardNo
     * @return
     */
    @ResponseBody
    @RequestMapping("/listCustomerByCardNo.action")
    public BaseOutput<?> listCustomerByCardNo(String cardNo) {
        BaseOutput<AccountSimpleResponseDto> cardOutput = this.cardRpc.getOneAccountCard(cardNo);
        if (!cardOutput.isSuccess() || Objects.isNull(cardOutput.getData()) || Objects.isNull(cardOutput.getData().getAccountInfo())) {
            return cardOutput;
        }
        CustomerQueryInput cq = new CustomerQueryInput();
        cq.setId(cardOutput.getData().getAccountInfo().getCustomerId());
        //获取当前登录人的市场，和客户市场进行对比
        //如果不相等，就直接返回
        if (!Objects.equals(SessionContext.getSessionContext().getUserTicket().getFirmId(), cardOutput.getData().getAccountInfo().getFirmId())) {
            return BaseOutput.failure("未查询到相关客户信息");
        }
        cq.setMarketId(cardOutput.getData().getAccountInfo().getFirmId());
        BaseOutput<List<Customer>> output = this.customerRpc.list(cq);
        return output;
    }

}
