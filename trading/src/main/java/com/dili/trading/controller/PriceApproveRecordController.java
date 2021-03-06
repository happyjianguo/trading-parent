package com.dili.trading.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
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
import com.dili.bpmc.sdk.domain.TaskMapping;
import com.dili.bpmc.sdk.rpc.restful.TaskRpc;
import com.dili.orders.constants.OrdersConstant;
import com.dili.orders.domain.PriceApproveRecord;
import com.dili.orders.dto.PriceApproveRecordQueryDto;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.util.BeanConver;
import com.dili.trading.component.BpmcUtil;
import com.dili.trading.dto.PriceApproveRecordProcessDto;
import com.dili.trading.rpc.PriceApproveRecordRpc;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.rpc.RoleRpc;
import com.dili.uap.sdk.session.SessionContext;

/**
 * PriceApproveRecordController
 */
@Controller
@RequestMapping("/priceApproveRecord")
public class PriceApproveRecordController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceApproveRecordController.class);
	@Autowired
	private PriceApproveRecordRpc priceApproveRpc;
	@Autowired
	private BpmcUtil bpmcUtil;
	@Autowired
	private TaskRpc taskRpc;
	@Autowired
	private RoleRpc roleRpc;

	/**
	 * ?????????
	 *
	 * @return
	 */
	@GetMapping("/index.html")
	public String index(ModelMap modelMap) {
		modelMap.put("weighingStartTime", LocalDate.now() + " 00:00:00");
		modelMap.put("weighingEndTime", LocalDate.now() + " 23:59:59");
		return "priceApproveRecord/index";
	}

	/**
	 * ????????????
	 *
	 * @param query
	 * @return
	 */
	@ResponseBody
	@PostMapping("/listPage.action")
	public String listPage(@RequestBody PriceApproveRecordQueryDto query) {

		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return JSON.toJSONString(BaseOutput.failure("???????????????"));
		}
		query.setMarketId(user.getFirmId());
		
		if (query.getWeighingStartTime() == null && query.getWeighingEndTime() == null) {
			query.setWeighingStartTime(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
			query.setWeighingEndTime(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));
		}

		if (query.getWeighingStartTime() != null && query.getWeighingEndTime() == null) {
			query.setWeighingEndTime(query.getWeighingStartTime().plusDays(366L).withHour(23).withMinute(59).withSecond(59));
		}

		if (query.getWeighingStartTime() == null && query.getWeighingEndTime() != null) {
			query.setWeighingStartTime(query.getWeighingEndTime().plusDays(-366L).withHour(0).withMinute(0).withSecond(0));
		}
		if (query.getWeighingEndTime().compareTo(LocalDateTime.now()) > 0) {
			query.setWeighingEndTime(LocalDateTime.now());
		}

		PageOutput<List<PriceApproveRecord>> output = this.priceApproveRpc.listPage(query);

		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return null;
		}

		List<PriceApproveRecordProcessDto> priceList = BeanConver.copyList(output.getData(), PriceApproveRecordProcessDto.class);
		this.bpmcUtil.fitLoggedUserIsCanHandledProcess(priceList);

		query.setMetadata(query.getMetadata());
		try {
			List<Map> list = ValueProviderUtils.buildDataByProvider(query, priceList);

			return new EasyuiPageOutput(output.getTotal(), list).toString();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * ????????????
	 * 
	 * @param businessKey ??????id
	 * @param taskId      ??????id
	 * @param isNeedClaim ??????????????????
	 * @param modal       ????????????
	 * @param modelMap
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/approve.html")
	public String approveView(@RequestParam Long businessKey, @RequestParam String taskId, @RequestParam(defaultValue = "false") Boolean isNeedClaim,
			@RequestParam(defaultValue = "false") Boolean modal, ModelMap modelMap) throws Exception {
		BaseOutput<PriceApproveRecord> output = this.priceApproveRpc.getById(businessKey);
		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return null;
		}
		Map<Object, Object> metadata = new HashMap<Object, Object>();
		metadata.put("tradeWeight", "weightProvider");

		metadata.put("unitPrice", "moneyProvider");
		metadata.put("referencePrice", "moneyProvider");

		metadata.put("tradeType", "tradeTypeCodeProvider");

		modelMap.addAttribute("model", ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData())).get(0)).addAttribute("taskId", taskId).addAttribute("isNeedClaim", isNeedClaim)
				.addAttribute("modal", modal);
		return "priceApproveRecord/approve";
	}

	/**
	 * ????????????
	 * 
	 * @param businessKey ??????id
	 * @param taskId      ??????id
	 * @param isNeedClaim ??????????????????
	 * @param modal       ????????????
	 * @param modelMap
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/appApprove.action")
	public BaseOutput<?> approveView(@RequestParam Long id) throws Exception {
		BaseOutput<PriceApproveRecord> output = this.priceApproveRpc.getById(id);
		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return null;
		}

		if (output.getData() == null) {
			return BaseOutput.failure("?????????????????????");

		}
		PriceApproveRecordProcessDto dto = BeanConver.copyBean(output.getData(), PriceApproveRecordProcessDto.class);
		this.bpmcUtil.fitLoggedUserIsCanHandledProcess(Arrays.asList(dto));
		return BaseOutput.successData(dto);
	}

	/**
	 * ????????????
	 * 
	 * @param id          id
	 * @param taskId      ????????????id
	 * @param isNeedClaim ??????????????????
	 * @param modelMap
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/approveAccept.action")
	public BaseOutput<Object> approveAccept(@RequestParam Long id, @RequestParam String notes, @RequestParam String taskId, @RequestParam Boolean isNeedClaim, ModelMap modelMap) throws Exception {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("???????????????");
		}
		if (isNeedClaim) {
			BaseOutput<String> output = this.taskRpc.claim(taskId, user.getId().toString());
			if (!output.isSuccess()) {
				return BaseOutput.failure("????????????????????????????????????????????????");
			}
		}
		return this.priceApproveRpc.approveAccept(id, user.getId(), notes, taskId);
	}

	/**
	 * ????????????
	 * 
	 * @param id          id
	 * @param taskId      ????????????id
	 * @param isNeedClaim ??????????????????
	 * @param modelMap
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/approveReject.action")
	public BaseOutput<Object> approveReject(@RequestParam Long id, @RequestParam String notes, @RequestParam String taskId, @RequestParam Boolean isNeedClaim, ModelMap modelMap) throws Exception {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("???????????????");
		}
		if (isNeedClaim) {
			BaseOutput<String> output = this.taskRpc.claim(taskId, user.getId().toString());
			if (!output.isSuccess()) {
				return BaseOutput.failure("????????????????????????");
			}
		}
		return this.priceApproveRpc.approveReject(id, user.getId(), notes, taskId);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param query
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listByLoggedUser.action")
	public BaseOutput<?> listByLoggedUser(PriceApproveRecordQueryDto query) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("???????????????");
		}
		if (query.getMarketId() == null) {
			query.setMarketId(user.getFirmId());
		}
		BaseOutput<List<TaskMapping>> taskOutput = this.taskRpc.listUserTask(user.getId(), OrdersConstant.PRICE_APPROVE_PROCESS_DEFINITION_KEY);
		if (!taskOutput.isSuccess()) {
			LOGGER.error(taskOutput.getMessage());
			return BaseOutput.failure("????????????????????????");
		}
		if (CollectionUtils.isEmpty(taskOutput.getData())) {
			return BaseOutput.success();
		}
		Set<String> processInstanceIds = new HashSet<String>(taskOutput.getData().size());
		taskOutput.getData().forEach(t -> processInstanceIds.add(t.getProcessInstanceId()));
		query.setProcessInstanceIds(new ArrayList<String>(processInstanceIds));
		PageOutput<List<PriceApproveRecord>> output = this.priceApproveRpc.listPageApp(query);

		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return BaseOutput.failure("??????????????????");
		}

		List<PriceApproveRecordProcessDto> priceList = BeanConver.copyList(output.getData(), PriceApproveRecordProcessDto.class);
		this.bpmcUtil.fitLoggedUserIsCanHandledProcess(priceList);

		HashMap<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("weighingTime", "datetimeProvider");
		metadata.put("tradeType", "tradeTypeCodeProvider");

		query.setMetadata(metadata);
		try {
			List<Map> list = ValueProviderUtils.buildDataByProvider(query, priceList);

			PageOutput page = new PageOutput<List<Map>>();
			page.setCode(ResultCode.OK).setData(list).setTotal(output.getTotal());
			return page;

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return BaseOutput.failure("??????????????????");
		}
	}
}
