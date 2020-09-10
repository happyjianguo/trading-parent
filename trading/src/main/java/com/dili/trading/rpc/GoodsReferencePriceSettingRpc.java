package com.dili.trading.rpc;


import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.ss.domain.BaseOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.List;

/**
 * Description: 品类参考价RPC
 *
 * @date:    2020/8/21
 * @author:   Seabert.Zhan
 */
@FeignClient(name = "order-service", contextId = "goodsReferencePriceSettingRpc", url = "${orderService.url:}")
public interface GoodsReferencePriceSettingRpc {
    /**
     * 根据参数查询数据
     *
     * @param goodsReferencePriceSetting
     * @return String
     * @throws Exception
     */
    @RequestMapping(value = "/api/goodsReferencePriceSetting/getAllGoods", method = {RequestMethod.POST})
    List<GoodsReferencePriceSetting> getAllGoods(@RequestBody GoodsReferencePriceSetting goodsReferencePriceSetting);

    /**
     * 根据参数查询数据
     *
     * @param goodsReferencePriceSetting
     * @return BaseOutput<GoodsReferencePriceSetting>
     * @throws Exception
     */
    @RequestMapping(value = "/api/goodsReferencePriceSetting/detail")
    BaseOutput<GoodsReferencePriceSetting> findDetailDtoById(@RequestBody GoodsReferencePriceSetting goodsReferencePriceSetting);

    /**
     * 新增品类参考价
     *
     * @param goodsReferencePriceSetting
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/goodsReferencePriceSetting/insert", method = {RequestMethod.POST})
    BaseOutput<GoodsReferencePriceSetting> insert(@RequestBody GoodsReferencePriceSetting goodsReferencePriceSetting);

    /**
     * 修改品类参考价
     *
     * @param goodsReferencePriceSetting
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/goodsReferencePriceSetting/update", method = {RequestMethod.POST})
    BaseOutput<GoodsReferencePriceSetting> update(@RequestBody GoodsReferencePriceSetting goodsReferencePriceSetting);
}
