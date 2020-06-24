package com.dili.trading.rpc;

import com.dili.order.domain.TransitionDepartureSettlement;
import com.dili.ss.domain.BaseOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "order-service", contextId = "transitionDepartureSettlementRpc", url = "localhost:8185")
public interface TransitionDepartureSettlementRpc {


    /**
     * @param transitionDepartureSettlement
     * @return String
     * @throws Exception
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/listPage", method = {RequestMethod.POST})
    String listPage(@RequestBody TransitionDepartureSettlement transitionDepartureSettlement);


    /**
     * 根据参数查询数据
     *
     * @param transitionDepartureSettlement
     * @return String
     * @throws Exception
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/listByQueryParams", method = {RequestMethod.POST})
    String listByQueryParams(@RequestBody TransitionDepartureSettlement transitionDepartureSettlement);

    /**
     * 新增TransitionDepartureSettlement
     *
     * @param transitionDepartureSettlement
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/insert", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureSettlement> insert(@RequestBody TransitionDepartureSettlement transitionDepartureSettlement);

    /**
     * 修改TransitionDepartureSettlement
     *
     * @param transitionDepartureSettlement
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/update", method = {RequestMethod.POST})
    BaseOutput update(@RequestBody TransitionDepartureSettlement transitionDepartureSettlement);


    /**
     * 定时任务，每天凌晨12点更新当天为结算的单子，支付状态更改为已关闭状态
     *
     * @param transitionDepartureSettlement
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/scheduleUpdate", method = {RequestMethod.POST})
    BaseOutput scheduleUpdate(@RequestBody TransitionDepartureSettlement transitionDepartureSettlement);
}
