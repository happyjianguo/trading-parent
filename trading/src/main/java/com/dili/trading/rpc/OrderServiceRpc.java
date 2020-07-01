package com.dili.trading.rpc;

import com.dili.order.domain.TransitionDepartureApply;
import com.dili.ss.domain.BaseOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "order-service", contextId = "orders", url = "localhost:8185")
public interface OrderServiceRpc {
	/**
	 * 根据客户id查询客户最新审批通过该的审批单，如果是未结算的，那带出结算单的相关信息，如果是已撤销，那就不带出
	 *
	 * @param transitionDepartureApply
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/transitionDepartureApply/getOneByCustomerID", method = { RequestMethod.POST })
	BaseOutput<TransitionDepartureApply> getOneByCustomerID(@RequestBody TransitionDepartureApply transitionDepartureApply);

}
