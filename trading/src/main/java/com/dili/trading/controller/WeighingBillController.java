package com.dili.trading.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.orders.domain.WeighingBill;
import com.dili.orders.dto.AccountSimpleResponseDto;
import com.dili.orders.dto.WeighingBillQueryDto;
import com.dili.orders.dto.WeighingBillUpdateDto;
import com.dili.orders.rpc.CardRpc;
import com.dili.orders.rpc.CategoryRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.trading.constants.TradingConstans;
import com.dili.trading.rpc.WeighingBillRpc;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.session.SessionContext;

/**
 * TransitionDepartureApplyController
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
	public BaseOutput<Object> update(@RequestBody WeighingBillUpdateDto weighingBill) {
		UserTicket user = SessionContext.getSessionContext().getUserTicket();
		if (user == null) {
			return BaseOutput.failure("用户未登录");
		}
		weighingBill.setModifierId(user.getId());
		return this.weighingBillRpc.update(weighingBill);
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
	 * 分页查询
	 * @param query
	 * @return
	 */
	@PostMapping("/listPage.action")
	public String listPage(@RequestBody WeighingBill query) {
		PageOutput<List<WeighingBill>> output = this.weighingBillRpc.listPage(query);
		return new EasyuiPageOutput(output.getTotal(), output.getData()).toString();
	}
}
