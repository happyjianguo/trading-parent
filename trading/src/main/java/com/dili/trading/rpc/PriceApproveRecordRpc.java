package com.dili.trading.rpc;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.dili.orders.domain.PriceApproveRecord;
import com.dili.orders.dto.PriceApproveRecordQueryDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;

@FeignClient(name = "order-service", contextId = "priceApproveRecord", url = "${orderService.url:}")
public interface PriceApproveRecordRpc {

	/**
	 * 通用分页查询
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "/api/priceApproveRecord/listPage", method = RequestMethod.POST)
	PageOutput<List<PriceApproveRecord>> listPage(@RequestBody PriceApproveRecordQueryDto query);

	/**
	 * app分页查询
	 * 
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "/api/priceApproveRecord/listPageApp", method = RequestMethod.POST)
	PageOutput<List<PriceApproveRecord>> listPageApp(@RequestBody PriceApproveRecordQueryDto query);

	@RequestMapping(value = "/api/priceApproveRecord/getById")
	BaseOutput<PriceApproveRecord> getById(@RequestParam(value = "id") Long id);

	/**
	 * 审批通过
	 * 
	 * @param id         id
	 * @param approverId 审批人id
	 * @param notes      说明
	 * @param taskId     流程任务id
	 * @return
	 */
	@RequestMapping(value = "/api/priceApproveRecord/approveAccept")
	BaseOutput<Object> approveAccept(@RequestParam(value = "id") Long id, @RequestParam(value = "approverId") Long approverId, @RequestParam(value = "notes") String notes, @RequestParam(value = "taskId") String taskId);

	/**
	 * 审批拒绝
	 * 
	 * @param id         id
	 * @param approverId 审批人id
	 * @param notes      说明
	 * @param taskId     流程任务id
	 * @return
	 */
	@RequestMapping(value = "/api/priceApproveRecord/approveReject")
	BaseOutput<Object> approveReject(@RequestParam(value = "id") Long id, @RequestParam(value = "approverId") Long approverId, @RequestParam(value = "notes") String notes, @RequestParam(value = "taskId") String taskId);
}
