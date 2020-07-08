package com.dili.trading.service;

import com.dili.orders.domain.WeighingBill;
import com.dili.orders.dto.WeighingBillUpdateDto;
import com.dili.ss.base.BaseService;
import com.dili.ss.domain.BaseOutput;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2020-06-19 14:20:28.
 */
public interface WeighingBillService extends BaseService<WeighingBill, Long> {

	/**
	 * 新增过磅单
	 * 
	 * @param weighingBill
	 * @return
	 */
	BaseOutput<String> addWeighingBill(WeighingBill weighingBill);

	/**
	 * 修改过磅单
	 * 
	 * @param weighingBill
	 * @return
	 */
	BaseOutput<Object> updateWeighingBill(WeighingBillUpdateDto weighingBill);

	/**
	 * 结算
	 * 
	 * @param serialNo       过磅单号
	 * @param buyerPassword  买家交易密码
	 * @param sellerPassword 卖家交易密码
	 * @param operatorId TODO
	 * @return
	 */
	BaseOutput<Object> settle(String serialNo, String buyerPassword, String sellerPassword, Long operatorId);

	/**
	 * 撤销过磅单
	 * 
	 * @param serialNo       过磅单号
	 * @param buyerPassword  买家交易密码
	 * @param sellerPassword 卖家交易密码
	 * @param operatorId TODO
	 * @return
	 */
	BaseOutput<Object> withdraw(String serialNo, String buyerPassword, String sellerPassword, Long operatorId);

	/**
	 * 作废过磅单
	 * 
	 * @param serialNo       过磅单号
	 * @param buyerPassword  买家交易密码
	 * @param sellerPassword 卖家交易密码
	 * @param operatorId TODO
	 * @return
	 */
	BaseOutput<Object> invalidate(String serialNo, String buyerPassword, String sellerPassword, Long operatorId);

	/**
	 * 关闭
	 * 
	 * @param serialNo 过磅单号
	 * @return
	 */
	BaseOutput<Object> close(String serialNo);
}