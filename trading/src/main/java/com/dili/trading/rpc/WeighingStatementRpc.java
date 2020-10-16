package com.dili.trading.rpc;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dili.orders.config.PayServiceFeignConfig;
import com.dili.orders.dto.WeighingStatementAppletDto;
import com.dili.orders.dto.WeighingStatementAppletQuery;
import com.dili.ss.domain.PageOutput;

@FeignClient(name = "order-service", contextId = "weighingStatement", url = "${orderService.url:}", configuration = PayServiceFeignConfig.class)
public interface WeighingStatementRpc {

	@RequestMapping(value = "/api/weighingStatement/listApplet", method = RequestMethod.POST)
	PageOutput<List<WeighingStatementAppletDto>> listApplet(@RequestBody WeighingStatementAppletQuery query);

}
