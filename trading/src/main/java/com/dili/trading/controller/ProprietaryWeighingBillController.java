package com.dili.trading.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.assets.sdk.dto.TradeTypeDto;
import com.dili.assets.sdk.dto.TradeTypeQuery;
import com.dili.assets.sdk.rpc.TradeTypeRpc;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.constants.OrdersConstant;
import com.dili.orders.constants.TradingConstans;
import com.dili.orders.domain.PaymentType;
import com.dili.orders.domain.TradingBillType;
import com.dili.orders.domain.WeighingBill;
import com.dili.orders.domain.WeighingStatement;
import com.dili.orders.domain.WeighingStatementState;
import com.dili.orders.dto.*;
import com.dili.orders.rpc.CardRpc;
import com.dili.orders.rpc.CategoryRpc;
import com.dili.orders.rpc.PayRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.exception.AppException;
import com.dili.ss.idempotent.annotation.Idempotent;
import com.dili.ss.idempotent.annotation.Token;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.redis.service.RedisDistributedLock;
import com.dili.ss.redis.service.RedisUtil;
import com.dili.trading.dto.TraceTradeBillResponseDto;
import com.dili.trading.dto.WeighingBillSaveAndSettleDto;
import com.dili.trading.rpc.AuthenticationRpc;
import com.dili.trading.rpc.FarmerWeghingBillRpc;
import com.dili.trading.rpc.ProprietaryWeighingBillRpc;
import com.dili.trading.rpc.QualityTraceRpc;
import com.dili.trading.rpc.WeighingBillRpc;
import com.dili.uap.sdk.domain.DataDictionaryValue;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.domain.dto.UserQuery;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.rpc.DataDictionaryRpc;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.rpc.UserRpc;
import com.dili.uap.sdk.session.SessionConstants;
import com.dili.uap.sdk.session.SessionContext;
import com.dili.uap.sdk.util.WebContent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * WeighingBillController
 */
@Controller
@RequestMapping("/proprietaryTradingBill")
public class ProprietaryWeighingBillController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProprietaryWeighingBillController.class);

	private static final String HEADER_CACHE_KEY = "trading_weighing_bill_header_cache:";

	private static final String WEIGHING_BILL_ID_LOCK_KEY_PRIFIX = "weighing_bill_lock_";

	private static final long WEIGHING_BILL_LOCK_EXPIRE = 60 * 10;

	@Autowired
	private ProprietaryWeighingBillRpc weighingBillRpc;
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
	private QualityTraceRpc qualityTraceRpc;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private RedisDistributedLock redisDistributedLock;
	@Autowired
	private WeighingBillRpc oweighingBillRpc;
	@Autowired
	private FarmerWeghingBillRpc farmerWeighingBillRpc;

	/**
	 * 列表页
	 *
	 * @return
	 */
	@GetMapping("/index.html")
	public String index(ModelMap modelMap) {
		modelMap.put("operationStartTime", LocalDate.now() + " 00:00:00");
		modelMap.put("operationEndTime", LocalDate.now() + " 23:59:59");
		return "proprietaryWeighingBill/index";
	}

	/**
	 * 结算
	 *
	 * @param weighingBill 过磅单和卖家密码数据
	 * @return
	 * @throws Exception
	 */
	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/saveAndSettle.action")
	public BaseOutput<?> saveAndSettle(@RequestBody WeighingBillSaveAndSettleDto weighingBill) throws Exception {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		BaseOutput<?> output = null;
		weighingBill.setTradingBillType(TradingBillType.PROPRIETARY.getValue());
		if (weighingBill.getPaymentType().equals(PaymentType.CARD.getValue())) {
			AccountPasswordValidateDto dto = new AccountPasswordValidateDto();
			dto.setAccountId(weighingBill.getBuyerAccount());
			dto.setPassword(weighingBill.getBuyerPassword());
			output = this.payRpc.validateAccountPassword(dto);
			if (!output.isSuccess()) {
				return output;
			}
		}
		WeighingStatement ws = null;
		if (weighingBill.getId() == null) {
			weighingBill.setCreatorId(user.getId());
			// 设置市场id
			weighingBill.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
			BaseOutput<WeighingStatement> wsOutput = this.weighingBillRpc.add(weighingBill);
			if (!wsOutput.isSuccess()) {
				return wsOutput;
			}
			ws = wsOutput.getData();
			BaseOutput<WeighingStatement> settlementOutput = this.weighingBillRpc.settle(ws.getWeighingBillId(), weighingBill.getBuyerPassword(), user.getId(), user.getFirmId());
			if (settlementOutput == null) {
				return BaseOutput.failure("请求服务器失败");
			}
			if (!settlementOutput.isSuccess()) {
				return settlementOutput;
			}
			ws = settlementOutput.getData();
		} else {
			String key = WEIGHING_BILL_ID_LOCK_KEY_PRIFIX + weighingBill.getId();
			boolean success = false;
			try {
				success = this.redisDistributedLock.tryGetLock(key, weighingBill.getId().toString(), WEIGHING_BILL_LOCK_EXPIRE);
				if (!success) {
					return BaseOutput.failure("锁定过磅单失败");
				}
				weighingBill.setModifierId(user.getId());
				BaseOutput<WeighingStatement> wsOutput = this.weighingBillRpc.update(weighingBill);
				if (!wsOutput.isSuccess()) {
					return wsOutput;
				}
				ws = wsOutput.getData();
				BaseOutput<WeighingStatement> settlementOutput = this.weighingBillRpc.settle(ws.getWeighingBillId(), weighingBill.getBuyerPassword(), user.getId(), user.getFirmId());
				if (!settlementOutput.isSuccess()) {
					return settlementOutput;
				}
				ws = settlementOutput.getData();
			} catch (Exception e) {
				return BaseOutput.failure(e.getMessage());
			} finally {
				if (success) {
					this.redisDistributedLock.releaseLock(key, weighingBill.getId().toString());
				}
			}
		}
		if (WeighingStatementState.FROZEN.getValue().equals(ws.getState())) {
			output = this.getWeighingBillPrintData(ws.getWeighingBillId(), false);
			if (!output.isSuccess()) {
				return output;
			}
			return output.setMessage("付款成功");
		}
		if (WeighingStatementState.PAID.getValue().equals(ws.getState())) {
			output = this.getWeighingStatementPrintData(ws.getSerialNo(), false);
			if (!output.isSuccess()) {
				return output;
			}
			return output.setMessage("付款成功");
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
//	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/withdraw.action")
	public BaseOutput<Object> withdraw(Long id, String buyerPassword, String sellerPassword) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		return this.weighingBillRpc.withdraw(id, buyerPassword, sellerPassword, user.getId());
	}

	/**
	 * 作废
	 *
	 * @param serialNo
	 * @param buyerPassword
	 * @param sellerPassword
	 * @return
	 */
//	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/invalidate.action")
	public BaseOutput<Object> invalidate(@RequestParam Long id, @RequestParam String buyerPassword, @RequestParam String sellerPassword) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		return this.weighingBillRpc.invalidate(id, buyerPassword, sellerPassword, user.getId());
	}

	/**
	 * 查询列表
	 *
	 * @param dto
	 * @return
	 */
	@ResponseBody
	@PostMapping("/listByExample.action")
	public BaseOutput<?> listByExample(@RequestBody WeighingBillQueryDto dto) {
		// 判断，如果是已结算的话，需要加入参数，操作员，如果是查询的结算的，只会有一个状态
		if (CollectionUtils.isNotEmpty(dto.getStatementStates()) && Objects.equals(dto.getStatementStates().size(), 1)) {
			dto.setOperatorId(SessionContext.getSessionContext().getUserTicket().getId());
		}
		// 需要加入市场
		if (Objects.isNull(dto.getMarketId())) {
			dto.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
		}
		if (CollectionUtils.isEmpty(dto.getDepartmentIds())) {
			List<Map> deptDataAuths = SessionContext.getSessionContext().dataAuth(DataAuthType.DEPARTMENT.getCode());
			if (CollectionUtils.isEmpty(deptDataAuths)) {
				return BaseOutput.success();
			}
			List<Long> departmentIds = new ArrayList<Long>(deptDataAuths.size());
			deptDataAuths.forEach(da -> departmentIds.add(Long.valueOf(da.get("value").toString())));
			dto.setDepartmentIds(departmentIds);
		}
		BaseOutput<List<WeighingBillClientListDto>> output = this.weighingBillRpc.listByExample(dto);
		if (!output.isSuccess()) {
			return output;
		}
		TradeTypeQuery tradeTypeQuery = new TradeTypeQuery();
		tradeTypeQuery.setPageNum(1);
		tradeTypeQuery.setPageSize(Integer.MAX_VALUE);
		List<TradeTypeDto> rows = this.tradeTypeRpc.query(tradeTypeQuery).getRows();
		output.getData().forEach(wb -> {
			TradeTypeDto target = rows.stream().filter(t -> t.getCode().equals(wb.getTradeType())).findFirst().orElse(null);
			if (target != null) {
				wb.setTradeTypeName(target.getName());
			}
		});
		return output;
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
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		CategoryDTO query = new CategoryDTO();
		query.setMarketId(user.getFirmId());
		query.setKeyword(keyword);
		return this.categoryRpc.getTree(query);
	}

	/**
	 * 查询包装类型
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listPackingType.action")
	public BaseOutput<?> listPackingType() {
		DataDictionaryValue query = DTOUtils.newInstance(DataDictionaryValue.class);
		query.setDdCode(OrdersConstant.PACKING_TYPE_DD_CODE);
		query.setFirmCode(OrdersConstant.SHOUGUANG_FIRM_CODE);
		return this.dataDictionaryRpc.listDataDictionaryValue(query);

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
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		BaseOutput<AccountSimpleResponseDto> output = this.cardRpc.getOneAccountCard(cardNo);
		if (!output.isSuccess()) {
			return BaseOutput.failure(output.getMessage());
		}
		if (!output.getData().getAccountInfo().getFirmId().equals(user.getFirmId())) {
			return BaseOutput.success();
		}
		BaseOutput<CustomerExtendDto> customerOutput = this.customerRpc.get(output.getData().getAccountInfo().getCustomerId(), user.getFirmId());
		if (!customerOutput.isSuccess()) {
			LOGGER.error(customerOutput.getMessage());
			return BaseOutput.failure("查询客户信息失败");
		}
		if (customerOutput.getData() == null) {
			return BaseOutput.failure("未查询到指定客户");
		}
		List<String> characterTypes = new ArrayList<String>(customerOutput.getData().getCharacterTypeList().size());
		customerOutput.getData().getCharacterTypeList().forEach(c -> characterTypes.add(c.getCharacterType()));
		output.getData().setCustomerCharacterTypes(characterTypes);
		output.getData().setBuyerRegionTag(customerOutput.getData().getCustomerMarket().getBusinessRegionTag());
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
	public String listPage(@RequestBody WeighingBillQueryDto query) {
		if (query.getTradingBillType() == null) {
			query.setTradingBillTypeList(Arrays.asList(TradingBillType.FARMER.getValue(), TradingBillType.PROPRIETARY.getValue()));
		}
		// 如果市场id为空，则加入
		if (Objects.isNull(query.getMarketId())) {
			query.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
		}

		if (query.getOperationStartTime() == null && query.getOperationEndTime() == null) {
			query.setOperationStartTime(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
			query.setOperationEndTime(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));
		}

		if (query.getOperationStartTime() != null && query.getOperationEndTime() == null) {
			query.setOperationEndTime(query.getOperationStartTime().plusDays(366L).withHour(23).withMinute(59).withSecond(59));
		}

		if (query.getOperationStartTime() == null && query.getOperationEndTime() != null) {
			query.setOperationStartTime(query.getOperationEndTime().plusDays(-366L).withHour(0).withMinute(0).withSecond(0));
		}
		if (query.getOperationEndTime().compareTo(LocalDateTime.now()) > 0) {
			query.setOperationEndTime(LocalDateTime.now());
		}

		List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
		if (CollectionUtils.isNotEmpty(ranges)) {
			String value = (String) ranges.get(0).get("value");
			// 如果value为0，则为个人
			if (value.equals("0")) {
				if (query.getOperatorId() == null) {
					query.setOperatorId(SessionContext.getSessionContext().getUserTicket().getId());
				} else {
					if (!query.getOperatorId().equals(SessionContext.getSessionContext().getUserTicket().getId())) {
						return new EasyuiPageOutput(0L, new ArrayList<Object>(0)).toString();
					}
				}
			}
		}

		PageOutput<List<WeighingBillListPageDto>> output = null;
		BaseOutput<WeighingBillPrintListDto> printListOutput = null;

		List<Map> deptDataAuths = SessionContext.getSessionContext().dataAuth(DataAuthType.DEPARTMENT.getCode());
		if (CollectionUtils.isEmpty(deptDataAuths)) {
			return new EasyuiPageOutput(0L, new ArrayList<>(0)).toString();
		}
		List<Long> departmentIds = new ArrayList<Long>(deptDataAuths.size());
		deptDataAuths.forEach(da -> departmentIds.add(Long.valueOf(da.get("value").toString())));
		query.setDepartmentIds(departmentIds);

		if (query.isExportData()) {
			printListOutput = this.oweighingBillRpc.printList(query);
			output = PageOutput.success().setData(printListOutput.getData().getPageList()).setTotal((long) printListOutput.getData().getPageList().size())
					.setPageNum(printListOutput.getData().getPageList().getPageNum()).setPageSize(printListOutput.getData().getPageList().getPageSize());
		} else {
			output = this.oweighingBillRpc.listPage(query);
		}

		if (!output.isSuccess()) {
			return null;
		}
		List<WeighingBillListPageDto> result = output.getData();
		List<Long> orderIds = result.stream().map(WeighingBillListPageDto::getId).collect(Collectors.toList());
		// 获取并拼装检测数据
		this.getQualityTrace(result, orderIds);

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
			List<Map> list = ValueProviderUtils.buildDataByProvider(query, result);

			if (printListOutput != null) {
				List<Map> statisticMap = ValueProviderUtils.buildDataByProvider(query, Arrays.asList(printListOutput.getData().getStatisticsDto()));
				list.add(statisticMap.get(0));
				return new EasyuiPageOutput(output.getTotal() + 1, list).toString();
			}

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
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		CustomerQueryInput cq = new CustomerQueryInput();
		cq.setKeyword(name);
		cq.setMarketId(user.getFirmId());
		BaseOutput<List<CustomerExtendDto>> output = this.customerRpc.list(cq);
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
		BaseOutput<List<CustomerExtendDto>> output = this.customerRpc.list(cq);
		if (!output.isSuccess()) {
			return output;
		}
		// 获取到客户
		CustomerExtendDto customer = output.getData().get(0);
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
	@GetMapping("/weighingStatement/detail.html")
	public String detail(Long id, ModelMap modelMap) {
		BaseOutput<WeighingBillDetailDto> output = this.weighingBillRpc.findDetailDtoByStatementId(id);
		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return this.index(modelMap);
		}
		this.getQualityTrace(output.getData());
		Map<Object, Object> metadata = new HashMap<Object, Object>();
		metadata.put("netWeight", "weightProvider");
		metadata.put("unitWeight", "weightProvider");
		metadata.put("roughWeight", "weightProvider");
		metadata.put("tareWeight", "weightProvider");
		metadata.put("estimatedNetWeight", "weightProvider");
		metadata.put("subtractionRate", "weightProvider");
		metadata.put("subtractionWeight", "weightProvider");
		metadata.put("createdTime", "datetimeProvider");
		metadata.put("weighingTime", "datetimeProvider");
		metadata.put("measureType", "measureTypeProvider");
		metadata.put("unitPrice", "moneyProvider");

		metadata.put("statement.tradeAmount", "moneyProvider");
		metadata.put("statement.frozenAmount", "moneyProvider");
		metadata.put("statement.buyerPoundage", "moneyProvider");
		metadata.put("statement.buyerActualAmount", "moneyProvider");
		metadata.put("statement.sellerPoundage", "moneyProvider");
		metadata.put("statement.sellerActualAmount", "moneyProvider");
		metadata.put("statement.state", "weighingStatementStateProvider");

		metadata.put("tradeType", "tradeTypeCodeProvider");
		try {
			List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData()));
			metadata = new HashMap<Object, Object>();
			metadata.put("operationTime", "datetimeProvider");
			List<Map> recordsListMap = ValueProviderUtils.buildDataByProvider(metadata, output.getData().getRecords());
			list.get(0).put("records", recordsListMap);
			modelMap.addAttribute("model", list.get(0));
			return "proprietaryWeighingBill/detail";
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
	@Token(url = "/proprietaryTradingBill/operatorInvalidate.action")
	@GetMapping("/operatorInvalidate.html")
	public String validatePassword(Long id, ModelMap modelMap) {
		WeighingBillQueryDto queryDto = new WeighingBillQueryDto();
		queryDto.setId(id);
		BaseOutput<WeighingBill> output = this.oweighingBillRpc.findById(id);
		if (!output.isSuccess()) {
			throw new AppException(output.getMessage());
		}
		modelMap.addAttribute("weighingBillId", id).addAttribute("tradingBillType", output.getData().getTradingBillType()).addAttribute("model", SessionContext.getSessionContext().getUserTicket())
				.addAttribute("submitHandler", "invalidateHandler");
		return "proprietaryWeighingBill/validatePassword";
	}

	/**
	 * 操作员撤销
	 *
	 * @param id
	 * @param operatorPassword
	 * @param modelMap
	 * @return
	 */
//	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/operatorInvalidate.action")
	public BaseOutput<Object> operatorInvalidate(@RequestParam Long id, @RequestParam String operatorPassword, @RequestParam Integer tradingBillType, HttpServletRequest request, ModelMap modelMap) {
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
		if (tradingBillType.equals(TradingBillType.FARMER.getValue())) {
			return this.farmerWeighingBillRpc.operatorInvalidate(id, user.getId());
		} else if (tradingBillType.equals(TradingBillType.PROPRIETARY.getValue())) {
			return this.weighingBillRpc.operatorInvalidate(id, user.getId());
		} else {
			return BaseOutput.failure("未知的单据类型");
		}
	}

	/**
	 * 操作员作废密码验证页面
	 *
	 * @param id
	 * @param modelMap
	 * @return
	 */
	@Token(url = "/proprietaryTradingBill/operatorWithdraw.action")
	@GetMapping("/operatorWithdraw.html")
	public String operatorWithdraw(Long id, ModelMap modelMap) {
		WeighingBillQueryDto queryDto = new WeighingBillQueryDto();
		queryDto.setId(id);
		BaseOutput<WeighingBill> output = this.oweighingBillRpc.findById(id);
		if (!output.isSuccess()) {
			throw new AppException(output.getMessage());
		}
		modelMap.addAttribute("weighingBillId", id).addAttribute("tradingBillType", output.getData().getTradingBillType()).addAttribute("model", SessionContext.getSessionContext().getUserTicket())
				.addAttribute("submitHandler", "withdrawHandler");
		return "proprietaryWeighingBill/validatePassword";
	}

	/**
	 * 操作员作废
	 *
	 * @param id
	 * @param operatorPassword
	 * @param modelMap
	 * @return
	 */
//	@Idempotent(Idempotent.HEADER)
	@ResponseBody
	@PostMapping("/operatorWithdraw.action")
	public BaseOutput<Object> operatorWithdraw(@RequestParam Long id, @RequestParam String operatorPassword, @RequestParam Integer tradingBillType, HttpServletRequest request, ModelMap modelMap) {
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
	public BaseOutput<?> getWeighingBillPrintData(@RequestParam Long id, @RequestParam(defaultValue = "false") Boolean reprint) throws Exception {
		BaseOutput<PrintTemplateDataDto<WeighingBillPrintDto>> output = this.weighingBillRpc.getWeighingBillPrintData(id);
		if (!output.isSuccess()) {
			return output;
		}
		if (output.getData() == null) {
			return BaseOutput.failure("数据不存在");
		}
		output.getData().getData().setReprint(reprint);
		Map<Object, Object> metadata = new HashMap<Object, Object>();
		metadata.put("tradeType", "tradeTypeCodeProvider");
		List<Map> listMap = ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData().getData()));
		Map map = listMap.get(0);
		return BaseOutput.successData(new PrintTemplateDataDto<Map>(output.getData().getTemplate(), map));
	}

	/**
	 * 获取结算单打印数据
	 *
	 * @param serialNo 结算单号
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/getWeighingStatementPrintData.action")
	public BaseOutput<?> getWeighingStatementPrintData(@RequestParam String serialNo, @RequestParam(defaultValue = "false") Boolean reprint) throws Exception {
		BaseOutput<PrintTemplateDataDto<WeighingStatementPrintDto>> output = this.weighingBillRpc.getWeighingStatementPrintData(serialNo);
		if (!output.isSuccess()) {
			return output;
		}
		if (output.getData() == null) {
			return BaseOutput.failure("数据不存在");
		}
		output.getData().getData().setReprint(reprint);
		Map<Object, Object> metadata = new HashMap<Object, Object>();
		metadata.put("tradeType", "tradeTypeCodeProvider");
		List<Map> listMap = ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData().getData()));
		Map map = listMap.get(0);
		return BaseOutput.successData(new PrintTemplateDataDto<Map>(output.getData().getTemplate(), map));
	}

	/**
	 * 缓存过磅单显示的列头配置
	 * 
	 * @param json 列头数组
	 * @return
	 */
	@ResponseBody
	@PostMapping("/getTableHeader.action")
	public BaseOutput<Object> getTableHeader() {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		String json = this.redisUtil.get(HEADER_CACHE_KEY + user.getId()).toString();
		if (StringUtils.isNotBlank(json)) {
			return BaseOutput.successData(JSON.parseObject(json, new TypeReference<List<String>>() {
			}));
		}
		return BaseOutput.success();
	}

	/**
	 * 获取检测结果
	 *
	 * @author miaoguoxin
	 * @date 2020/11/6
	 */
	private void getQualityTrace(List<WeighingBillListPageDto> result, List<Long> orderIds) {
		BaseOutput<List<TraceTradeBillResponseDto>> listBaseOutput = qualityTraceRpc.queryByOrderIdList(orderIds);
		if (listBaseOutput.isSuccess() && CollectionUtil.isNotEmpty(listBaseOutput.getData())) {
			Map<Long, TraceTradeBillResponseDto> traceMap = listBaseOutput.getData().stream()
					.collect(Collectors.toMap(TraceTradeBillResponseDto::getBillId, Function.identity(), (key1, key2) -> key2));
			result.forEach(dto -> {
				TraceTradeBillResponseDto resDto = traceMap.get(dto.getId());
				if (resDto != null) {
					dto.setDetectStateDesc(resDto.getDetectStateDesc());
					dto.setLatestPdResult(resDto.getLatestPdResult());
				}
			});
		}
	}

	/**
	 * 获取详情页结果
	 *
	 * @author miaoguoxin
	 * @date 2020/11/6
	 */
	private void getQualityTrace(WeighingBillDetailDto dto) {
		BaseOutput<List<TraceTradeBillResponseDto>> listBaseOutput = qualityTraceRpc.queryByOrderIdList(Arrays.asList(dto.getId()));
		if (listBaseOutput.isSuccess() && CollectionUtil.isNotEmpty(listBaseOutput.getData())) {
			TraceTradeBillResponseDto resDto = listBaseOutput.getData().get(0);
			if (resDto != null) {
				dto.setDetectStateDesc(resDto.getDetectStateDesc());
				dto.setLatestPdResult(resDto.getLatestPdResult());
			}
		}
	}
}
