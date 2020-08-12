package com.dili.trading.rpc;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.dili.orders.domain.WeighingBill;
import com.dili.orders.dto.WeighingBillDetailDto;
import com.dili.orders.dto.WeighingBillListPageDto;
import com.dili.orders.dto.WeighingBillQueryDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;

@FeignClient(name = "order-service", contextId = "weighingBill")
public interface WeighingBillRpc {

	@RequestMapping(value = "/api/weighingBill/insert")
	BaseOutput<String> add(@RequestBody WeighingBill weighingBill);

	@RequestMapping(value = "/api/weighingBill/update")
	BaseOutput<Object> update(@RequestBody WeighingBill weighingBill);

	@RequestMapping(value = "/api/weighingBill/settle")
	BaseOutput<Object> settle(@RequestParam String serialNo, @RequestParam String buyerPassword, @RequestParam Long operatorId);

	@RequestMapping(value = "/api/weighingBill/withdraw")
	BaseOutput<Object> withdraw(@RequestParam String serialNo, @RequestParam String buyerPassword, @RequestParam String sellerPassword, @RequestParam Long operatorId);

	@RequestMapping(value = "/api/weighingBill/invalidate")
	BaseOutput<Object> invalidate(@RequestParam String serialNo, @RequestParam String buyerPassword, @RequestParam String sellerPassword, @RequestParam Long operatorId);

	@RequestMapping(value = "/api/weighingBill/close")
	BaseOutput<Object> close(@RequestParam String serialNo);

	@RequestMapping(value = "/api/weighingBill/listByExample", method = RequestMethod.POST)
	BaseOutput<Object> listByExample(@RequestBody WeighingBillQueryDto queryDto);

	@RequestMapping(value = "/api/weighingBill/listPage", method = RequestMethod.POST)
	PageOutput<List<WeighingBillListPageDto>> listPage(@RequestBody WeighingBillQueryDto query);

	@RequestMapping(value = "/api/weighingBill/detail")
	BaseOutput<WeighingBillDetailDto> findDetailDtoById(@RequestParam Long id);

	@RequestMapping(value = "/api/weighingBill/operatorInvalidate")
	BaseOutput<Object> operatorInvalidate(@RequestParam Long id, @RequestParam Long operatorId, @RequestParam String operatorPassword);

	@RequestMapping(value = "/api/weighingBill/operatorWithdraw")
	BaseOutput<Object> operatorWithdraw(@RequestParam Long id, @RequestParam Long operatorId, @RequestParam String operatorPassword);

}
