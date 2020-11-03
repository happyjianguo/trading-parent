package com.dili.trading.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dili.assets.sdk.rpc.TradeTypeRpc;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.dto.WeighingStatementAppletQuery;
import com.dili.orders.rpc.CardRpc;
import com.dili.orders.rpc.CategoryRpc;
import com.dili.orders.rpc.PayRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.AuthenticationRpc;
import com.dili.trading.rpc.WeighingBillRpc;
import com.dili.trading.rpc.WeighingStatementRpc;
import com.dili.uap.sdk.rpc.DataDictionaryRpc;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.rpc.UserRpc;

/**
 * WeighingBillController
 */
@Controller
@RequestMapping("/weighingStatement")
public class WeighingStatementController {

	private static final Logger LOGGER = LoggerFactory.getLogger(WeighingStatementController.class);
	@Autowired
	private WeighingBillRpc weighingBillRpc;
	@Autowired
	private CategoryRpc categoryRpc;
	@Autowired
	private FirmRpc firmRpc;
	@Autowired
	private CardRpc cardRpc;
	@Autowired
	private CustomerRpc customerRpc;
	@Autowired
	private UserRpc useRpc;
	@Autowired
	private PayRpc payRpc;
	@Autowired
	private DataDictionaryRpc dataDictionaryRpc;
	@Autowired
	private UserRpc userRpc;
	@Autowired
	private AuthenticationRpc authRpc;
	@Autowired
	private TradeTypeRpc tradeTypeRpc;
	@Autowired
	private WeighingStatementRpc statementRpc;

	/**
	 * 分页查询
	 *
	 * @param query
	 * @return
	 */
	@ResponseBody
	@PostMapping("/listByApplet.action")
	public BaseOutput<?> listApplet(@RequestBody WeighingStatementAppletQuery query) {
		return this.statementRpc.listApplet(query);
	}

}
