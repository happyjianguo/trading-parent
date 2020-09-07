package com.dili.trading.controller;

import com.alibaba.fastjson.JSONObject;
import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.assets.sdk.rpc.CategoryRpc;
import com.dili.orders.domain.ComprehensiveFee;
import com.dili.orders.rpc.CardRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.ComprehensiveFeeRpc;
import com.dili.trading.rpc.ReferencePriceRpc;
import com.dili.trading.service.ComprehensiveFeeService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 获取参考价controller
 *
 * @author Henry.Huang
 * @date   2020-08-18
 */
@Controller
@RequestMapping("/refrencePrice")
public class RefrencePriceController {

    @Autowired
    private ReferencePriceRpc referencePriceRpc;

    /**
     * 获取参考价
     * @param goodsId
     * @param marketId
     * @return
     */
    @RequestMapping(value = "/getReferencePriceByGoodsId.action", method = {RequestMethod.POST})
    @ResponseBody
    public BaseOutput getReferencePriceByGoodsId(Long goodsId,Long marketId) {
        return referencePriceRpc.getReferencePriceByGoodsId(goodsId,marketId);
    }


}
