package com.dili.trading.rpc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.dili.orders.domain.WeighingBill;
import com.dili.orders.dto.WeighingBillQueryDto;
import com.dili.orders.dto.WeighingBillUpdateDto;
import com.dili.ss.domain.BaseOutput;

@FeignClient(name = "order-service", contextId = "weighingBill", url = "localhost:8185")
public interface WeighingBillRpc {

	@RequestMapping(value = "/api/weighingBill/insert")
	BaseOutput<String> add(@RequestBody WeighingBill weighingBill);

	@RequestMapping(value = "/api/weighingBill/update")
	BaseOutput<Object> update(@RequestBody WeighingBillUpdateDto weighingBill);

	@RequestMapping(value = "/api/weighingBill/settle")
	BaseOutput<Object> settle(@RequestParam String serialNo, @RequestParam String buyerPassword, @RequestParam String sellerPassword, @RequestParam Long operatorId);

	@RequestMapping(value = "/api/weighingBill/withdraw")
	BaseOutput<Object> withdraw(@RequestParam String serialNo, @RequestParam String buyerPassword, @RequestParam String sellerPassword, @RequestParam Long operatorId);

	@RequestMapping(value = "/api/weighingBill/invalidate")
	BaseOutput<Object> invalidate(@RequestParam String serialNo, @RequestParam String buyerPassword, @RequestParam String sellerPassword, @RequestParam Long operatorId);

	@RequestMapping(value = "/api/weighingBill/close")
	BaseOutput<Object> close(@RequestParam String serialNo);

	@RequestMapping(value = "/api/weighingBill/listByExample", method = RequestMethod.POST)
	BaseOutput<Object> listByExample(@RequestBody WeighingBillQueryDto queryDto);
}
