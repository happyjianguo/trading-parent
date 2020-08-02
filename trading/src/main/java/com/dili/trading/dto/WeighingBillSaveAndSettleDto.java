package com.dili.trading.dto;

import com.dili.orders.domain.WeighingBill;

public class WeighingBillSaveAndSettleDto extends WeighingBill {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3262121233136817434L;

	private String buyerPassword;

	public String getBuyerPassword() {
		return buyerPassword;
	}

	public void setBuyerPassword(String buyerPassword) {
		this.buyerPassword = buyerPassword;
	}

}
