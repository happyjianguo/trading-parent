package com.dili.trading.rpc;

import com.dili.orders.domain.TransitionDepartureSettlement;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import org.apache.juli.logging.Log;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
    PageOutput<List<TransitionDepartureSettlement>> listByQueryParams(TransitionDepartureSettlement transitionDepartureSettlement);

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


    /**
     * 定时任务，每天凌晨12点更新当天为结算的单子，支付状态更改为已关闭状态
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/getOneById/{id}", method = {RequestMethod.GET})
    BaseOutput<TransitionDepartureSettlement> getOneById(@PathVariable(value = "id") Long id);


    /**
     * 新增结算单
     *
     * @param transitionDepartureSettlement
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/insertTransitionDepartureSettlement", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureSettlement> insertTransitionDepartureSettlement(@RequestBody TransitionDepartureSettlement transitionDepartureSettlement);

    /**
     * 结算单支付
     *
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/pay", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureSettlement> pay(@RequestParam(value = "id") Long id, @RequestParam(value = "password") String password, @RequestParam(value = "marketId") Long marketId, @RequestParam(value = "departmentId") Long departmentId, @RequestParam(value = "operatorCode") String operatorCode, @RequestParam(value = "operatorId") Long operatorId, @RequestParam(value = "operatorName") String operatorName, @RequestParam(value = "operatorUserName") String operatorUserName);

    /**
     * 撤销
     *
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/revocator", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureSettlement> revocator(@RequestBody TransitionDepartureSettlement transitionDepartureSettlement);

    /**
     * 掉用计费规则
     *
     * @param netWeight
     * @param marketId
     * @param departmentId
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureSettlement/fee", method = {RequestMethod.POST})
    BaseOutput getFee(@RequestParam(value = "netWeight") BigDecimal netWeight, @RequestParam(value = "marketId") Long marketId, @RequestParam(value = "departmentId") Long departmentId, @RequestParam(value = "id") Long id);
}
