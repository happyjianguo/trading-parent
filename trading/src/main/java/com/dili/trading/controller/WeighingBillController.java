package com.dili.trading.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.customer.sdk.domain.Customer;
import com.dili.customer.sdk.domain.dto.CustomerQueryInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.constants.OrdersConstant;
import com.dili.orders.constants.TradingConstans;
import com.dili.orders.domain.MeasureType;
import com.dili.orders.domain.WeighingStatement;
import com.dili.orders.domain.WeighingStatementState;
import com.dili.orders.dto.AccountPasswordValidateDto;
import com.dili.orders.dto.AccountSimpleResponseDto;
import com.dili.orders.dto.UserAccountCardResponseDto;
import com.dili.orders.dto.WeighingBillDetailDto;
import com.dili.orders.dto.WeighingBillListPageDto;
import com.dili.orders.dto.WeighingBillPrintDto;
import com.dili.orders.dto.WeighingBillQueryDto;
import com.dili.orders.dto.WeighingStatementPrintDto;
import com.dili.orders.rpc.CardRpc;
import com.dili.orders.rpc.CategoryRpc;
import com.dili.orders.rpc.PayRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.idempotent.annotation.Idempotent;
import com.dili.ss.idempotent.annotation.Token;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.dto.WeighingBillSaveAndSettleDto;
import com.dili.trading.rpc.AuthenticationRpc;
import com.dili.trading.rpc.WeighingBillRpc;
import com.dili.uap.sdk.domain.DataDictionaryValue;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.domain.dto.UserQuery;
import com.dili.uap.sdk.rpc.DataDictionaryRpc;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.rpc.UserRpc;
import com.dili.uap.sdk.session.SessionConstants;
import com.dili.uap.sdk.session.SessionContext;
import com.dili.uap.sdk.util.WebContent;

/**
 * WeighingBillController
 */
@Controller
@RequestMapping("/weighingBill")
public class WeighingBillController {

	private static final Logger LOGGER = LoggerFactory.getLogger(WeighingBillController.class);
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

	/**
	 * 列表页
	 *
	 * @return
	 */
	@GetMapping("/index.html")
	public String index(ModelMap modelMap) {
		modelMap.put("createdStart", LocalDate.now() + " 00:00:00");
		modelMap.put("createdEnd", LocalDate.now() + " 23:59:59");
		return "weighingBill/index";
	}

	/**
	 * 结算
	 *
	 * @param weighingBill 过磅单和卖家密码数据
	 * @return
	 */
	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/saveAndSettle.action")
	public BaseOutput<?> saveAndSettle(@RequestBody WeighingBillSaveAndSettleDto weighingBill) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		AccountPasswordValidateDto dto = new AccountPasswordValidateDto();
		dto.setAccountId(weighingBill.getBuyerAccount());
		dto.setPassword(weighingBill.getBuyerPassword());
		BaseOutput<?> output = this.payRpc.validateAccountPassword(dto);
		if (!output.isSuccess()) {
			return output;
		}
		if (StringUtils.isBlank(weighingBill.getSerialNo())) {
			weighingBill.setCreatorId(user.getId());
			// 设置市场id
			weighingBill.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
			output = this.weighingBillRpc.add(weighingBill);
			if (!output.isSuccess()) {
				return output;
			}
			output = this.weighingBillRpc.settle(output.getData().toString(), weighingBill.getBuyerPassword(), user.getId(), user.getFirmId());
		} else {
			weighingBill.setModifierId(user.getId());
			output = this.weighingBillRpc.update(weighingBill);
			if (!output.isSuccess()) {
				return output;
			}
			output = this.weighingBillRpc.settle(weighingBill.getSerialNo(), weighingBill.getBuyerPassword(), user.getId(), user.getFirmId());
		}
		return output;
	}

	/**
	 * 撤销
	 *
	 * @param serialNo
	 * @param buyerPassword
	 * @param sellerPassword
	 * @return
	 */
	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/withdraw.action")
	public BaseOutput<Object> withdraw(String serialNo, String buyerPassword, String sellerPassword) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		return this.weighingBillRpc.withdraw(serialNo, buyerPassword, sellerPassword, user.getId());
	}

	/**
	 * 作废
	 *
	 * @param serialNo
	 * @param buyerPassword
	 * @param sellerPassword
	 * @return
	 */
	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/invalidate.action")
	public BaseOutput<Object> invalidate(String serialNo, String buyerPassword, String sellerPassword) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		return this.weighingBillRpc.invalidate(serialNo, buyerPassword, sellerPassword, user.getId());
	}

	/**
	 * 查询列表
	 *
	 * @param dto
	 * @return
	 */
	@ResponseBody
	@PostMapping("/listByExample.action")
	public BaseOutput<Object> listByExample(@RequestBody WeighingBillQueryDto dto) {
		// 判断，如果是已结算的话，需要加入参数，操作员，如果是查询的结算的，只会有一个状态
		if (Objects.equals(dto.getStatementStates().size(), 1)) {
			dto.setOperatorId(SessionContext.getSessionContext().getUserTicket().getId());
		}
		// 需要加入市场
		if (Objects.isNull(dto.getMarketId())) {
			dto.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
		}
		return this.weighingBillRpc.listByExample(dto);
	}

	/**
	 * 根据快捷吗查询商品
	 *
	 * @param keyword 快捷码
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getGoodsByKeyword.action")
	public BaseOutput<List<CategoryDTO>> getGoodsByKeyword(String keyword) {
		BaseOutput<Firm> output = this.firmRpc.getByCode(TradingConstans.SHOUGUANG_FIRM_CODE);
		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return BaseOutput.failure("查询市场信息失败");
		}
		if (output.getData() == null) {
			return BaseOutput.failure("市场信息不存在");
		}
		CategoryDTO query = new CategoryDTO();
		query.setMarketId(output.getData().getId());
		query.setKeyword(keyword);
		return this.categoryRpc.getTree(query);
	}

	/**
	 * 根据卡号查询客户信息
	 *
	 * @param cardNo 卡号
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCustomerInfoByCardNo.action")
	public BaseOutput<AccountSimpleResponseDto> getCustomerInfoByCardNo(String cardNo) {
		BaseOutput<AccountSimpleResponseDto> output = this.cardRpc.getOneAccountCard(cardNo);
		return output;
	}

	/**
	 * 自动关闭过磅单
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/autoClose.api")
	public BaseOutput<Object> autoClose() {
		return this.weighingBillRpc.autoClose();
	}

	/**
	 * 分页查询
	 *
	 * @param query
	 * @return
	 */
	@ResponseBody
	@PostMapping("/listPage.action")
	public String listPage(WeighingBillQueryDto query) {
		// 如果市场id为空，则加入
		if (Objects.isNull(query.getMarketId())) {
			query.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
		}
		// 判断是否选择了操作员，如果选择了操作员，那就增加状态为已结算
		if (Objects.nonNull(query.getOperatorId())) {
			query.setStatementStates(Arrays.asList(WeighingStatementState.PAID.getValue()));
		}
		PageOutput<List<WeighingBillListPageDto>> output = this.weighingBillRpc.listPage(query);
		output.getData().forEach(wb -> {
			if (wb.getMeasureType().equals(MeasureType.WEIGHT.getValue())) {
				wb.setUnitPrice(wb.getUnitPrice() * 2);
			}
		});

//		Map<Object, Object> metadata = new HashMap<Object, Object>();
//		metadata.put("roughWeight", "weightProvider");
//		metadata.put("tareWeight", "weightProvider");
//		metadata.put("netWeight", "weightProvider");
//		metadata.put("unitWeight", "weightProvider");
//
//		metadata.put("unitPrice", "moneyProvider");
//		metadata.put("statement.tradeAmount", "moneyProvider");
//		metadata.put("statement.buyerPoundage", "moneyProvider");
//		metadata.put("statement.buyerActualAmount", "moneyProvider");
//		metadata.put("statement.sellerPoundage", "moneyProvider");
//		metadata.put("statement.sellerActualAmount", "moneyProvider");
//		metadata.put("statement.state", "weighingStatementStateProvider");
//
//		metadata.put("operationRecord.operationTime", "datetimeProvider");
//
//		JSONObject ddProvider = new JSONObject();
//		ddProvider.put(ValueProvider.PROVIDER_KEY, "dataDictionaryValueProvider");
//		ddProvider.put(ValueProvider.QUERY_PARAMS_KEY, "{\"dd_code\":\"trade_type\"}");
//		metadata.put("tradeType", ddProvider);

//		query.setMetadata(metadata);
		try {
			List<Map> list = ValueProviderUtils.buildDataByProvider(query, output.getData());

			return new EasyuiPageOutput(output.getTotal(), list).toString();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 查询列表
	 *
	 * @param query
	 * @return
	 */
	@ResponseBody
	@PostMapping("/list.action")
	public String list(WeighingBillQueryDto query) {
		// 如果市场id为空，则加入
		if (Objects.isNull(query.getMarketId())) {
			query.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
		}
		// 判断是否选择了操作员，如果选择了操作员，那就增加状态为已结算
		if (Objects.nonNull(query.getOperatorId())) {
			query.setStatementStates(Arrays.asList(WeighingStatementState.PAID.getValue()));
		}
		PageOutput<List<WeighingBillListPageDto>> output = this.weighingBillRpc.listPage(query);

		Map<Object, Object> metadata = new HashMap<Object, Object>();
		metadata.put("roughWeight", "weightProvider");
		metadata.put("tareWeight", "weightProvider");
		metadata.put("netWeight", "weightProvider");
		metadata.put("unitWeight", "weightProvider");

		metadata.put("unitPrice", "moneyProvider");
		metadata.put("statement.tradeAmount", "moneyProvider");
		metadata.put("statement.buyerPoundage", "moneyProvider");
		metadata.put("statement.buyerActualAmount", "moneyProvider");
		metadata.put("statement.sellerPoundage", "moneyProvider");
		metadata.put("statement.sellerActualAmount", "moneyProvider");
		metadata.put("statement.state", "weighingStatementStateProvider");

		metadata.put("operationRecord.operationTime", "datetimeProvider");

		JSONObject ddProvider = new JSONObject();
		ddProvider.put(ValueProvider.PROVIDER_KEY, "dataDictionaryValueProvider");
		ddProvider.put(ValueProvider.QUERY_PARAMS_KEY, "{\"dd_code\":\"trade_type\"}");
		metadata.put("tradeType", ddProvider);

		query.setMetadata(metadata);
		try {
			List<Map> list = ValueProviderUtils.buildDataByProvider(query, output.getData());

			return new EasyuiPageOutput(output.getTotal(), list).toString();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 查询客户信息
	 *
	 * @param name
	 * @param keyword
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listCustomerByKeyword.action")
	public BaseOutput<?> listCustomerByKeyword(String name, String keyword) {
		CustomerQueryInput cq = new CustomerQueryInput();
		cq.setKeyword(name);
		BaseOutput<Firm> firmOutput = this.firmRpc.getByCode(TradingConstans.SHOUGUANG_FIRM_CODE);
		if (!firmOutput.isSuccess()) {
			return firmOutput;
		}
		cq.setMarketId(firmOutput.getData().getId());
		BaseOutput<List<Customer>> output = this.customerRpc.list(cq);
		return output;
	}

	/**
	 * 查询客户信息
	 *
	 * @param
	 * @param
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listCustomerByCardNo.action")
	public BaseOutput<?> listCustomerByCardNo(String cardNo) {
		BaseOutput<AccountSimpleResponseDto> cardOutput = this.cardRpc.getOneAccountCard(cardNo);
		if (!cardOutput.isSuccess()) {
			return cardOutput;
		}
		CustomerQueryInput cq = new CustomerQueryInput();
		cq.setId(cardOutput.getData().getAccountInfo().getCustomerId());
		BaseOutput<Firm> firmOutput = this.firmRpc.getByCode(TradingConstans.SHOUGUANG_FIRM_CODE);
		if (!firmOutput.isSuccess()) {
			return firmOutput;
		}
		cq.setMarketId(firmOutput.getData().getId());
		BaseOutput<List<Customer>> output = this.customerRpc.list(cq);
		if (!output.isSuccess()) {
			return output;
		}
		// 获取到客户
		Customer customer = output.getData().get(0);
		UserAccountCardResponseDto accountInfo = cardOutput.getData().getAccountInfo();
		accountInfo.setCustomerName(customer.getName());
		return BaseOutput.successData(accountInfo);
	}

	/**
	 * 查询结算员信息
	 *
	 * @param name
	 * @param keyword
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listOperatorByKeyword.action")
	public BaseOutput<?> listOperatorByKeyword(String name, String keyword) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		UserQuery userQuery = DTOUtils.newInstance(UserQuery.class);
		userQuery.setFirmCode(user.getFirmCode());
		userQuery.setKeyword(keyword);
		return this.useRpc.listByExample(userQuery);
	}

	/**
	 * 详情页
	 *
	 * @param id
	 * @param modelMap
	 * @return
	 */
	@GetMapping("/detail.html")
	public String detail(Long id, ModelMap modelMap) {
		BaseOutput<WeighingBillDetailDto> output = this.weighingBillRpc.findDetailDtoById(id);
		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return this.index(modelMap);
		}
		Map<Object, Object> metadata = new HashMap<Object, Object>();
		metadata.put("netWeight", "weightProvider");
		metadata.put("unitWeight", "weightProvider");
		metadata.put("roughWeight", "weightProvider");
		metadata.put("tareWeight", "weightProvider");
		metadata.put("estimatedNetWeight", "weightProvider");
		metadata.put("subtractionRate", "weightProvider");
		metadata.put("subtractionWeight", "weightProvider");
		metadata.put("createdTime", "datetimeProvider");
		metadata.put("measureType", "measureTypeProvider");
		metadata.put("state", "weighingBillStateProvider");

		metadata.put("unitPrice", "moneyProvider");
		metadata.put("statement.tradeAmount", "moneyProvider");
		metadata.put("statement.buyerPoundage", "moneyProvider");
		metadata.put("statement.buyerActualAmount", "moneyProvider");
		metadata.put("statement.sellerPoundage", "moneyProvider");
		metadata.put("statement.sellerActualAmount", "moneyProvider");

		JSONObject ddProvider = new JSONObject();
		ddProvider.put(ValueProvider.PROVIDER_KEY, "dataDictionaryValueProvider");
		ddProvider.put(ValueProvider.QUERY_PARAMS_KEY, "{\"dd_code\":\"trade_type\"}");
		metadata.put("tradeType", ddProvider);
		// 未结算的，不展示结算信息
		if (Objects.equals(output.getData().getState(), 1)) {
			output.getData().setStatement(new WeighingStatement());
		}
		try {
			List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData()));
			metadata = new HashMap<Object, Object>();
			metadata.put("operationTime", "datetimeProvider");
			List<Map> recordsListMap = ValueProviderUtils.buildDataByProvider(metadata, output.getData().getRecords());
			list.get(0).put("records", recordsListMap);
			modelMap.addAttribute("model", list.get(0));
			return "weighingBill/detail";
		} catch (Exception e) {
			e.printStackTrace();
			return this.index(modelMap);
		}

	}

	/**
	 * 操作员撤销验证密码页面
	 *
	 * @param id
	 * @param modelMap
	 * @return
	 */
	@Token(url = "/weighingBill/operatorInvalidate.action")
	@GetMapping("/operatorInvalidate.html")
	public String validatePassword(Long id, ModelMap modelMap) {
		modelMap.addAttribute("weighingBillId", id).addAttribute("model", SessionContext.getSessionContext().getUserTicket()).addAttribute("submitHandler", "invalidateHandler");
		return "weighingBill/validatePassword";
	}

	/**
	 * 操作员撤销
	 *
	 * @param id
	 * @param operatorPassword
	 * @param modelMap
	 * @return
	 */
	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/operatorInvalidate.action")
	public BaseOutput<Object> operatorInvalidate(Long id, String operatorPassword, HttpServletRequest request, ModelMap modelMap) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		// 校验操作员密码
		BaseOutput<Object> pwdOutput = this.userRpc.validatePassword(user.getId(), operatorPassword);
		if (!pwdOutput.isSuccess()) {
			if (pwdOutput.getData() != null) {
				JSONObject jsonObj = JSON.parseObject(pwdOutput.getData().toString());
				// 如果用户被锁定则强制下线
				if (jsonObj.getBooleanValue("locked")) {
					BaseOutput<Object> logoutOutput = this.authRpc.loginout(new HashMap<String, String>() {
						{
							put("sessionId", getSessionId(request));
						}
					});
					if (!logoutOutput.isSuccess()) {
						return BaseOutput.failure("密码错误次数超限，强制注销登录失败");
					}
					return BaseOutput.successData(new HashMap<String, Object>() {
						{
							put("locked", true);
						}
					});
				}
			}
			return pwdOutput;
		}
		BaseOutput<Object> output = this.weighingBillRpc.operatorInvalidate(id, user.getId());
		return output;
	}

	/**
	 * 操作员作废密码验证页面
	 *
	 * @param id
	 * @param modelMap
	 * @return
	 */
	@Token(url = "/weighingBill/operatorWithdraw.action")
	@GetMapping("/operatorWithdraw.html")
	public String operatorWithdraw(Long id, ModelMap modelMap) {
		modelMap.addAttribute("weighingBillId", id).addAttribute("model", SessionContext.getSessionContext().getUserTicket()).addAttribute("submitHandler", "withdrawHandler");
		return "weighingBill/validatePassword";
	}

	/**
	 * 操作员作废
	 *
	 * @param id
	 * @param operatorPassword
	 * @param modelMap
	 * @return
	 */
	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/operatorWithdraw.action")
	public BaseOutput<Object> operatorWithdraw(Long id, String operatorPassword, HttpServletRequest request, ModelMap modelMap) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		// 校验操作员密码
		BaseOutput<Object> pwdOutput = this.userRpc.validatePassword(user.getId(), operatorPassword);
		if (!pwdOutput.isSuccess()) {
			if (pwdOutput.getData() != null) {
				JSONObject jsonObj = JSON.parseObject(pwdOutput.getData().toString());
				// 如果用户被锁定则强制下线
				if (jsonObj.getBooleanValue("locked")) {
					BaseOutput<Object> logoutOutput = this.authRpc.loginout(new HashMap<String, String>() {
						{
							put("sessionId", getSessionId(request));
						}
					});
					if (!logoutOutput.isSuccess()) {
						return BaseOutput.failure("密码错误次数超限，强制注销登录失败");
					}
					return BaseOutput.successData(new HashMap<String, Object>() {
						{
							put("locked", true);
						}
					});
				}
			}
			return pwdOutput;
		}
		BaseOutput<Object> output = this.weighingBillRpc.operatorWithdraw(id, user.getId());
		return output;
	}

	/**
	 * 获取登录用户sessionId
	 * 
	 * @param req
	 * @return
	 */
	private String getSessionId(HttpServletRequest req) {
		// 首先读取链接中的session
		String sessionId = req.getParameter(SessionConstants.SESSION_ID);
		if (StringUtils.isBlank(sessionId)) {
			sessionId = req.getHeader(SessionConstants.SESSION_ID);
		}
		if (StringUtils.isNotBlank(sessionId)) {
			WebContent.setCookie(SessionConstants.SESSION_ID, sessionId);
		} else {
			sessionId = WebContent.getCookieVal(SessionConstants.SESSION_ID);
		}
		return sessionId;
	}

	/**
	 * 获取取重按钮是否可用
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getFetchWeightSwitch.action")
	public BaseOutput<Object> getFetchWeightSwitch() {
		DataDictionaryValue ddValueQuery = DTOUtils.newInstance(DataDictionaryValue.class);
		ddValueQuery.setFirmCode(OrdersConstant.SHOUGUANG_FIRM_CODE);
		ddValueQuery.setDdCode(OrdersConstant.FETCH_WEIGHT_SWITCH_DD_CODE);
		BaseOutput<List<DataDictionaryValue>> output = this.dataDictionaryRpc.listDataDictionaryValueWithFirm(ddValueQuery);
		if (!output.isSuccess()) {
			return BaseOutput.failure(output.getMessage());
		}
		return BaseOutput.successData(Boolean.valueOf(output.getData().get(0).getName()));
	}

	/**
	 * 获取过磅单打印数据
	 *
	 * @param serialNo 过磅单编号
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/getWeighingBillPrintData.action")
	public BaseOutput<?> getWeighingBillPrintData(@RequestParam String serialNo, @RequestParam(defaultValue = "false") Boolean reprint) throws Exception {
		BaseOutput<WeighingBillPrintDto> output = this.weighingBillRpc.getWeighingBillPrintData(serialNo);
		if (!output.isSuccess()) {
			return output;
		}
		output.getData().setReprint(reprint);
		JSONObject ddProvider = new JSONObject();
		ddProvider.put(ValueProvider.PROVIDER_KEY, "dataDictionaryValueProvider");
		ddProvider.put(ValueProvider.QUERY_PARAMS_KEY, "{\"dd_code\":\"trade_type\"}");
		Map<Object, Object> metadata = new HashMap<Object, Object>();
		metadata.put("tradeType", ddProvider);
		List<Map> listMap = ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData()));
		return BaseOutput.successData(listMap.get(0));
	}

	/**
	 * 获取结算单打印数据
	 *
	 * @param serialNo 结算单号
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getWeighingStatementPrintData.action")
	public BaseOutput<WeighingStatementPrintDto> getWeighingStatementPrintData(@RequestParam String serialNo, @RequestParam(defaultValue = "false") Boolean reprint) {
		BaseOutput<WeighingStatementPrintDto> output = this.weighingBillRpc.getWeighingStatementPrintData(serialNo);
		if (!output.isSuccess()) {
			return output;
		}
		output.getData().setReprint(reprint);
		return output;
	}
}
