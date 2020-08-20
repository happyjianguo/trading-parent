package com.dili.trading.service;

import com.dili.orders.domain.ComprehensiveFee;
import com.dili.ss.domain.BaseOutput;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2020-06-19 14:20:28.
 * @author  Henry.Huang
 */
public interface ComprehensiveFeeService {

	/**
	 * 新增
	 * @param comprehensiveFee
	 * @return
	 */
	BaseOutput<ComprehensiveFee> insertComprehensiveFee(ComprehensiveFee comprehensiveFee);

	/**
	 * 缴费，需要调用支付系统收钱，并且更改对应申请单的支付状态为已支付
	 * @param id
	 * @param password
	 * @return
	 */
	BaseOutput pay(Long id, String password);

}