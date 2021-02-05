package com.dili.trading.rpc;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.dili.orders.config.FeignHeaderConfig;
import com.dili.orders.domain.WeighingBill;
import com.dili.orders.domain.WeighingStatement;
import com.dili.orders.dto.PrintTemplateDataDto;
import com.dili.orders.dto.WeighingBillClientListDto;
import com.dili.orders.dto.WeighingBillDetailDto;
import com.dili.orders.dto.WeighingBillListPageDto;
import com.dili.orders.dto.WeighingBillPrintDto;
import com.dili.orders.dto.WeighingBillPrintListDto;
import com.dili.orders.dto.WeighingBillQueryDto;
import com.dili.orders.dto.WeighingStatementPrintDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;

@FeignClient(name = "order-service", contextId = "proprietaryWeighingBill", url = "${orderService.url:}", configuration = FeignHeaderConfig.class)
public interface ProprietaryWeighingBillRpc {

	@RequestMapping(value = "/api/proprietaryWeighingBill/insert")
	BaseOutput<WeighingStatement> add(@RequestBody WeighingBill weighingBill);

	@RequestMapping(value = "/api/proprietaryWeighingBill/update")
	BaseOutput<WeighingStatement> update(@RequestBody WeighingBill weighingBill);

	@RequestMapping(value = "/api/proprietaryWeighingBill/settle")
	BaseOutput<WeighingStatement> settle(@RequestParam(value = "id") Long id, @RequestParam(value = "buyerPassword", required = false) String buyerPassword,
			@RequestParam(value = "operatorId") Long operatorId, @RequestParam(value = "marketId") Long marketId);

	@RequestMapping(value = "/api/proprietaryWeighingBill/withdraw")
	BaseOutput<Object> withdraw(@RequestParam(value = "id") Long id, @RequestParam(value = "buyerPassword") String buyerPassword, @RequestParam(value = "sellerPassword") String sellerPassword,
			@RequestParam(value = "operatorId") Long operatorId);

	@RequestMapping(value = "/api/proprietaryWeighingBill/invalidate")
	BaseOutput<Object> invalidate(@RequestParam(value = "id") Long id, @RequestParam(value = "buyerPassword") String buyerPassword, @RequestParam(value = "sellerPassword") String sellerPassword,
			@RequestParam(value = "operatorId") Long operatorId);

	@RequestMapping(value = "/api/proprietaryWeighingBill/autoClose")
	BaseOutput<Object> autoClose();

	@RequestMapping(value = "/api/proprietaryWeighingBill/listByExample", method = RequestMethod.POST)
	BaseOutput<List<WeighingBillClientListDto>> listByExample(@RequestBody WeighingBillQueryDto queryDto);

	@RequestMapping(value = "/api/proprietaryWeighingBill/listPage", method = RequestMethod.POST)
	PageOutput<List<WeighingBillListPageDto>> listPage(@RequestBody WeighingBillQueryDto query);

	@RequestMapping(value = "/api/proprietaryWeighingBill/printList", method = RequestMethod.POST)
	BaseOutput<WeighingBillPrintListDto> printList(@RequestBody WeighingBillQueryDto query);

	@RequestMapping(value = "/api/weighingStatement/detail")
	BaseOutput<WeighingBillDetailDto> findDetailDtoByStatementId(@RequestParam(value = "id") Long id);

	@RequestMapping(value = "/api/proprietaryWeighingBill/operatorInvalidate")
	BaseOutput<Object> operatorInvalidate(@RequestParam(value = "id") Long id, @RequestParam(value = "operatorId") Long operatorId);

	@RequestMapping(value = "/api/proprietaryWeighingBill/operatorWithdraw")
	BaseOutput<Object> operatorWithdraw(@RequestParam(value = "id") Long id, @RequestParam(value = "operatorId") Long operatorId);

	@RequestMapping(value = "/api/proprietaryWeighingBill/getWeighingBillPrintData")
	BaseOutput<PrintTemplateDataDto<WeighingBillPrintDto>> getWeighingBillPrintData(@RequestParam(value = "id") Long id);

	@RequestMapping(value = "/api/proprietaryWeighingBill/getWeighingStatementPrintData")
	BaseOutput<PrintTemplateDataDto<WeighingStatementPrintDto>> getWeighingStatementPrintData(@RequestParam(value = "serialNo") String serialNo);
}
