package com.dili.trading.rpc;

import com.dili.ss.domain.BaseOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 获取参考价RPC
 *
 *@author Henry.Huang
 *@date 2020/08/20
 *
 */
@FeignClient(name = "order-service", contextId = "referencePriceRpc", url = "${orderService.url:}")
public interface ReferencePriceRpc {


    /**
     * 获取参考价
     * @param goodsId
     * @param marketId
     * @param tradeType
     * @return
     */
    @RequestMapping(value = "/api/refrencePrice/getReferencePriceByGoodsId", method = {RequestMethod.POST})
    BaseOutput<Object> getReferencePriceByGoodsId(@RequestParam(value = "goodsId") Long goodsId, @RequestParam(value = "marketId") Long marketId, @RequestParam(value = "tradeType") String tradeType, @RequestParam(value = "type") Integer type);


}
