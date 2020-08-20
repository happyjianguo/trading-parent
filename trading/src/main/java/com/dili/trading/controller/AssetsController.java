package com.dili.trading.controller;

import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.orders.rpc.AssetsRpc;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 模糊查询商品
 * @author  Henry.Huang
 * @date 2020/08/20
 */
@RequestMapping("/assets")
@Controller
public class AssetsController {

    @Autowired
    AssetsRpc assetsRpc;

    /**
     * 商品查询
     *
     * @return
     */
    @RequestMapping(value = "/listNormal.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public List<CategoryDTO> listNormal(String keyword) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setMarketId(userTicket.getFirmId());
        categoryDTO.setKeyword(keyword);
        List<CategoryDTO> list = assetsRpc.list(categoryDTO).getData();
        return list;
    }
}
