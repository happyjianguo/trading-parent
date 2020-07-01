package com.dili.trading.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dili.order.domain.WeighingBill;
import com.dili.order.dto.WeighingBillQueryDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.WeighingBillRpc;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;

/**
 * TransitionDepartureApplyController
 */
@Controller
@RequestMapping("/weighingBill")
public class WeighingBillController {

	@Autowired
	private WeighingBillRpc weighingBillRpc;

	/**
	 * 新增过磅单
	 * 
	 * @param weighingBill
	 * @return
	 */
	@ResponseBody
	@PostMapping("/add.action")
	public BaseOutput<String> add(@RequestBody WeighingBill weighingBill) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		weighingBill.setCreatorId(user.getId());
		return this.weighingBillRpc.add(weighingBill);
	}

	/**
	 * 修改过磅单
	 * 
	 * @param weighingBill
	 * @return
	 */
	@ResponseBody
	@PostMapping("/update.action")
	public BaseOutput<String> update(@RequestBody WeighingBill weighingBill) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		weighingBill.setModifierId(user.getId());
		return this.weighingBillRpc.add(weighingBill);
	}

	/**
	 * 结算
	 * 
	 * @param serialNo
	 * @param buyerPassword
	 * @param sellerPassword
	 * @return
	 */
	@ResponseBody
	@PostMapping("/settle.action")
	public BaseOutput<Object> settle(String serialNo, String buyerPassword, String sellerPassword) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		return this.weighingBillRpc.settle(serialNo, buyerPassword, sellerPassword, user.getId());
	}

	/**
	 * 撤销
	 * 
	 * @param serialNo
	 * @param buyerPassword
	 * @param sellerPassword
	 * @return
	 */
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
		return this.weighingBillRpc.listByExample(dto);
	}

}
