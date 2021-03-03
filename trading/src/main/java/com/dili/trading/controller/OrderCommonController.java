package com.dili.trading.controller;

import com.alibaba.fastjson.JSONObject;
import com.dili.orders.dto.AccountSimpleResponseDto;
import com.dili.orders.dto.UserAccountCardResponseDto;
import com.dili.orders.rpc.CardRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公共接口
 */
@RestController
@RequestMapping("/orderCommon")
public class OrderCommonController {

    @Autowired
    private CardRpc cardRpc;

    /**
     * 根据客户id查询卡号
     *
     * @param customerId
     * @return
     */

    @RequestMapping("/cardList.action")
    public BaseOutput getCardNoByCustomerId(Long customerId) {
        Long[] longs = new Long[1];
        longs[0] = customerId;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("customerIds", longs);
        jsonObject.put("firmId", SessionContext.getSessionContext().getUserTicket().getFirmId());
        return cardRpc.getList(jsonObject);
    }

    /**
     * 根据卡号获取账户相关信息
     *
     * @param cardNo
     * @return
     */
    @RequestMapping("/oneByCardNo.action")
    public BaseOutput oneByCardNo(String cardNo) {
        return cardRpc.getOneAccountCard(cardNo);
    }
}
