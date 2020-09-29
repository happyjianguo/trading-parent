package com.dili.trading.controller;

import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.assets.sdk.dto.CusCategoryDTO;
import com.dili.assets.sdk.dto.CusCategoryQuery;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 模糊查询商品
 *
 * @author Henry.Huang
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
    public List<CusCategoryDTO> listNormal(String keyword) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        CusCategoryQuery categoryDTO = new CusCategoryQuery();
        categoryDTO.setMarketId(userTicket.getFirmId());
        if (StringUtils.isNotBlank(keyword)) {
            categoryDTO.setKeyword(keyword);
        }
        categoryDTO.setState(EnabledStateEnum.ENABLED.getCode());
        List<CusCategoryDTO> list = assetsRpc.listCusCategory(categoryDTO).getData();
        return list;
    }
}
