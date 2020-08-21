package com.dili.trading.service;

import com.dili.orders.domain.ComprehensiveFee;
import com.dili.ss.domain.BaseOutput;

/**
 * 检测收费服务
 *
 * @author  Henry.Huang
 * @date  2020/08/20
 */
public interface ComprehensiveFeeService {

	/**
	 * 根据申请单信息，新增一条检测收费单信息
	 *
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