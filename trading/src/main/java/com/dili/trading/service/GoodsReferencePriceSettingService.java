package com.dili.trading.service;

import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.ss.domain.BaseOutput;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2020-06-19 14:20:28.
 * @author Seabert.Zhan
 */
public interface GoodsReferencePriceSettingService {

	/**
	 * 新增品类参考价
	 *
	 * @param goodsReferencePriceSetting
	 * @return BaseOutput<GoodsReferencePriceSetting>
	 */
	BaseOutput<GoodsReferencePriceSetting> insertGoodsReferencePriceSetting(GoodsReferencePriceSetting goodsReferencePriceSetting);

	/**
	 * 修改品类参考价
	 *
	 * @param goodsReferencePriceSetting
	 * @return BaseOutput<GoodsReferencePriceSetting>
	 */
	BaseOutput<GoodsReferencePriceSetting> updateGoodsReferencePriceSetting(GoodsReferencePriceSetting goodsReferencePriceSetting);
}