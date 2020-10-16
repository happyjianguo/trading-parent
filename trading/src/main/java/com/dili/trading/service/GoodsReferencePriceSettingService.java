package com.dili.trading.service;

import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.dto.GoodsReferencePriceQueryDto;

import java.util.List;

/**
 * Description: 品类参考价接口GoodsReferencePriceSettingService
 *
 * @date:    2020/8/21
 * @author:   Seabert.Zhan
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

	/**
	* 根据父节点查询列表
	* @author miaoguoxin
	* @date 2020/10/15
	*/
	List<GoodsReferencePriceSetting> getListByParent(GoodsReferencePriceQueryDto params);

	/**
	* 条件查询列表
	* @author miaoguoxin
	* @date 2020/10/15
	*/
	List<GoodsReferencePriceSetting> getList(GoodsReferencePriceQueryDto params);
}
