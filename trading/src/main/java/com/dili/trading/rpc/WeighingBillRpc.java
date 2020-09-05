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

@FeignClient(name = "order-service", contextId = "weighingBill", url = "${orderService.url:}")
public interface WeighingBillRpc {

	@RequestMapping(value = "/api/weighingBill/insert")
	BaseOutput<String> add(@RequestBody WeighingBill weighingBill);

	@RequestMapping(value = "/api/weighingBill/update")
	BaseOutput<Object> update(@RequestBody WeighingBill weighingBill);

	@RequestMapping(value = "/api/weighingBill/settle")
	BaseOutput<Object> settle(@RequestParam(value = "serialNo") String serialNo, @RequestParam(value = "buyerPassword") String buyerPassword, @RequestParam(value = "operatorId") Long operatorId,
			@RequestParam(value = "marketId") Long marketId);

	@RequestMapping(value = "/api/weighingBill/withdraw")
	BaseOutput<Object> withdraw(@RequestParam(value = "serialNo") String serialNo, @RequestParam(value = "buyerPassword") String buyerPassword,
			@RequestParam(value = "sellerPassword") String sellerPassword, @RequestParam(value = "operatorId") Long operatorId);

	@RequestMapping(value = "/api/weighingBill/invalidate")
	BaseOutput<Object> invalidate(@RequestParam(value = "serialNo") String serialNo, @RequestParam(value = "buyerPassword") String buyerPassword,
			@RequestParam(value = "sellerPassword") String sellerPassword, @RequestParam(value = "operatorId") Long operatorId);

	@RequestMapping(value = "/api/weighingBill/autoClose")
	BaseOutput<Object> autoClose();

	@RequestMapping(value = "/api/weighingBill/listByExample", method = RequestMethod.POST)
	BaseOutput<Object> listByExample(@RequestBody WeighingBillQueryDto queryDto);

	@RequestMapping(value = "/api/weighingBill/listPage", method = RequestMethod.POST)
	PageOutput<List<WeighingBillListPageDto>> listPage(@RequestBody WeighingBillQueryDto query);

	@RequestMapping(value = "/api/weighingBill/detail")
	BaseOutput<WeighingBillDetailDto> findDetailDtoById(@RequestParam(value = "id") Long id);

	@RequestMapping(value = "/api/weighingBill/operatorInvalidate")
	BaseOutput<Object> operatorInvalidate(@RequestParam(value = "id") Long id, @RequestParam(value = "operatorId") Long operatorId, @RequestParam(value = "operatorPassword") String operatorPassword);

	@RequestMapping(value = "/api/weighingBill/operatorWithdraw")
	BaseOutput<Object> operatorWithdraw(@RequestParam(value = "id") Long id, @RequestParam(value = "operatorId") Long operatorId, @RequestParam(value = "operatorPassword") String operatorPassword);

	@RequestMapping(value = "/api/weighingBill/getWeighingBillPrintData")
	BaseOutput<Object> getWeighingBillPrintData(@RequestParam(value = "serialNo") String serialNo);

	@RequestMapping(value = "/api/weighingBill/getWeighingStatementPrintData")
	BaseOutput<Object> getWeighingStatementPrintData(@RequestParam(value = "serialNo") String serialNo);
}
