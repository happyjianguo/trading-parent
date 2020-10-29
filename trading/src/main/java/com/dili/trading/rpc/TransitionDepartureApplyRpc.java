package com.dili.trading.rpc;

import com.dili.orders.config.PayServiceFeignConfig;
import com.dili.orders.domain.TransitionDepartureApply;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-service", contextId = "transitionDepartureApplyRpc", url = "${orderService.url:}", configuration = PayServiceFeignConfig.class)
public interface TransitionDepartureApplyRpc {

    /**
     * 分页查询TransitionDepartureApply，返回easyui分页信息
     *
     * @param transitionDepartureApply
     * @return String
     */
    @RequestMapping(value = "/api/transitionDepartureApply/listPage", method = {RequestMethod.POST})
    String listPage(@RequestBody TransitionDepartureApply transitionDepartureApply);

    /**
     * 根据客户id查询客户最新审批通过该的审批单，如果是未结算的，那带出结算单的相关信息，如果是已撤销，那就不带出
     *
     * @param transitionDepartureApply
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureApply/getOneByCustomerCardNo", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureApply> getOneByCustomerCardNo(@RequestBody TransitionDepartureApply transitionDepartureApply, @RequestParam(value = "marketId") Long marketId, @RequestParam(value = "departmentId") Long departmentId);

    /**
     * 新增TransitionDepartureApply
     *
     * @param transitionDepartureApply
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/transitionDepartureApply/insert", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureApply> insert(@RequestBody TransitionDepartureApply transitionDepartureApply);


    /**
     * 修改TransitionDepartureApply
     *
     * @param transitionDepartureApply
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/transitionDepartureApply/update", method = {RequestMethod.POST})
    BaseOutput update(@RequestBody TransitionDepartureApply transitionDepartureApply);


    @RequestMapping(value = "/api/transitionDepartureApply/getOneByID/{id}", method = {RequestMethod.GET})
    BaseOutput<TransitionDepartureApply> getOneByID(@PathVariable(value = "id") Long id);

    /**
     * 根据参数查询数据
     *
     * @param transitionDepartureApply
     * @return String
     */
    @RequestMapping(value = "/api/transitionDepartureApply/listByQueryParams", method = {RequestMethod.POST})
    PageOutput<List<TransitionDepartureApply>> listByQueryParams(@RequestBody TransitionDepartureApply transitionDepartureApply);

    /**
     * 根据客户卡号查询到当天的所有的已审核的单子
     *
     * @param transitionDepartureApply
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureApply/listByCustomerCardNo", method = {RequestMethod.POST})
    BaseOutput<?> listByCustomerCardNo(@RequestBody TransitionDepartureApply transitionDepartureApply);

    /**
     * 根据id查询申请单和结算单信息
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/transitionDepartureApply/getApplyAndSettleById", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureApply> getApplyAndSettleById(@RequestBody TransitionDepartureApply transitionDepartureApply, @RequestParam(value = "marketId") Long marketId, @RequestParam(value = "departmentId") Long departmentId);

    /**
     * app根据id查询
     * @param id
     * @return
     */
    @RequestMapping(value = "/getOneByIdForApp", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureApply> getOneByIdForApp(@RequestParam(value = "id") Long id);
}
