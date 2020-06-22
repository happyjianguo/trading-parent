package com.dili.trading.rpc;

import com.dili.order.domain.TransitionDepartureApply;
import com.dili.ss.domain.BaseOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "order-service", contextId = "transitionDepartureApplyRpc",url = "localhost:8185")
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
    @RequestMapping(value = "/api/transitionDepartureApply/getOneByCustomerID", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureApply> getOneByCustomerID(@RequestBody TransitionDepartureApply transitionDepartureApply);

    /**
     * 新增TransitionDepartureApply
     *
     * @param transitionDepartureApply
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/transitionDepartureApply/insert", method = {RequestMethod.POST})
    BaseOutput insert(@RequestBody TransitionDepartureApply transitionDepartureApply);


    /**
     * 修改TransitionDepartureApply
     *
     * @param transitionDepartureApply
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/transitionDepartureApply/update", method = {RequestMethod.POST})
    BaseOutput update(@RequestBody TransitionDepartureApply transitionDepartureApply);


    @RequestMapping(value = "/api/transitionDepartureApply/getOneByID", method = {RequestMethod.POST})
    BaseOutput<TransitionDepartureApply> getOneByID(Long id);

    /**
     * 根据参数查询数据
     *
     * @param transitionDepartureApply
     * @return String
     */
    @RequestMapping(value = "/api/transitionDepartureApply/listByQueryParams", method = {RequestMethod.POST})
    String listByQueryParams(@RequestBody TransitionDepartureApply transitionDepartureApply);
}
