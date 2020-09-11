package com.dili.trading.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.ReferencePriceRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
     * @param tradeType
     * @return
     */
    @RequestMapping(value = "/getReferencePriceByGoodsId.action", method = {RequestMethod.POST})
    @ResponseBody
    public BaseOutput<Object> getReferencePriceByGoodsId(Long goodsId, Long marketId, String tradeType) {
        return referencePriceRpc.getReferencePriceByGoodsId(goodsId, marketId, tradeType);
    }


}
