package com.dili.trading.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.dili.bpmc.sdk.dto.TaskDto;
import com.dili.bpmc.sdk.rpc.TaskRpc;
import com.dili.orders.constants.OrdersConstant;
import com.dili.orders.domain.PriceApproveRecord;
import com.dili.orders.dto.PriceApproveRecordQueryDto;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.util.BeanConver;
import com.dili.trading.component.BpmcUtil;
import com.dili.trading.dto.PriceApproveRecordProcessDto;
import com.dili.trading.rpc.PriceApproveRecordRpc;
import com.dili.uap.sdk.domain.Role;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.rpc.RoleRpc;
import com.dili.uap.sdk.rpc.UserRpc;
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
	 * 列表页
	 *
	 * @return
	 */
	@GetMapping("/index.html")
	public String index(ModelMap modelMap) {
		modelMap.put("createdStart", LocalDate.now() + " 00:00:00");
		modelMap.put("createdEnd", LocalDate.now() + " 23:59:59");
		return "priceApproveRecord/index";
	}

	/**
	 * 分页查询
	 *
	 * @param query
	 * @return
	 */
	@ResponseBody
	@PostMapping("/listPage.action")
	public String listPage(PriceApproveRecordQueryDto query) {

		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return JSON.toJSONString(BaseOutput.failure("用户未登录"));
		}
		query.setMarketId(user.getFirmId());

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
	 * 审批试图
	 * 
	 * @param businessKey 业务id
	 * @param taskId      任务id
	 * @param isNeedClaim 是否需要签收
	 * @param modal       是否窗口
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
	 * 审批通过
	 * 
	 * @param id          id
	 * @param taskId      流程任务id
	 * @param isNeedClaim 是否需要签收
	 * @param modelMap
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/approveAccept.action")
	public BaseOutput<Object> approveAccept(@RequestParam Long id, @RequestParam String notes, @RequestParam String taskId, @RequestParam Boolean isNeedClaim, ModelMap modelMap) throws Exception {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		if (isNeedClaim) {
			BaseOutput<String> output = this.taskRpc.claim(taskId, user.getId().toString());
			if (!output.isSuccess()) {
				return BaseOutput.failure("签收流程任务失败，该任务已被签收");
			}
		}
		return this.priceApproveRpc.approveAccept(id, user.getId(), notes, taskId);
	}

	/**
	 * 审批拒绝
	 * 
	 * @param id          id
	 * @param taskId      流程任务id
	 * @param isNeedClaim 是否需要签收
	 * @param modelMap
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/approveReject.action")
	public BaseOutput<Object> approveReject(@RequestParam Long id, @RequestParam String notes, @RequestParam String taskId, @RequestParam Boolean isNeedClaim, ModelMap modelMap) throws Exception {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		if (isNeedClaim) {
			BaseOutput<String> output = this.taskRpc.claim(taskId, user.getId().toString());
			if (!output.isSuccess()) {
				return BaseOutput.failure("签收流程任务失败");
			}
		}
		return this.priceApproveRpc.approveReject(id, user.getId(), notes, taskId);
	}

	/**
	 * 查询登录用户审批数据
	 * 
	 * @param query
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listByLoggedUser.action")
	public BaseOutput<?> listByLoggedUser(PriceApproveRecordQueryDto query) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		BaseOutput<List<TaskMapping>> taskOutput = this.taskRpc.listUserTask(user.getId(), OrdersConstant.PRICE_APPROVE_PROCESS_DEFINITION_KEY);
		if (!taskOutput.isSuccess()) {
			LOGGER.error(taskOutput.getMessage());
			return BaseOutput.failure("查询流程任务失败");
		}
		List<String> processInstanceIds = new ArrayList<String>(taskOutput.getData().size());
		taskOutput.getData().forEach(t -> processInstanceIds.add(t.getProcessInstanceId()));
		query.setProcessInstanceIds(processInstanceIds);
		PageOutput<List<PriceApproveRecord>> output = this.priceApproveRpc.listPage(query);

		if (!output.isSuccess()) {
			LOGGER.error(output.getMessage());
			return BaseOutput.failure("查询数据失败");
		}

		List<PriceApproveRecordProcessDto> priceList = BeanConver.copyList(output.getData(), PriceApproveRecordProcessDto.class);
		this.bpmcUtil.fitLoggedUserIsCanHandledProcess(priceList);

		query.setMetadata(query.getMetadata());
		try {
			List<Map> list = ValueProviderUtils.buildDataByProvider(query, priceList);

			PageOutput page = new PageOutput<List<Map>>();
			page.setCode(ResultCode.OK).setData(list).setTotal(output.getTotal());
			return page;

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return BaseOutput.failure("查询数据失败");
		}
	}
}
