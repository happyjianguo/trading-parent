package com.dili.trading.service;

import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.orders.dto.ReferencePriceSettingRequestDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.dto.GoodsReferencePriceQueryDto;
import com.dili.trading.dto.ReferenceSettingResponseDto;

import java.util.List;

/**
 * Description: 品类参考价接口GoodsReferencePriceSettingService
 *
 * @date:    2020/8/21
 * @author:   Seabert.Zhan
 */
public interface GoodsReferencePriceSettingService {

	/**
	* 保存/编辑设置
	* @author miaoguoxin
	* @date 2021/2/1
	*/
	void saveOrEdit(ReferencePriceSettingRequestDto requestDto);

	/**
	* 详情
	* @author miaoguoxin
	* @date 2021/2/1
	*/
	ReferenceSettingResponseDto getDetail(Long goodId);

	/**
	*  根据父节点查询列表
	* @author miaoguoxin
	* @date 2021/2/2
	*/
	List<ReferenceSettingResponseDto> getListByParentV2(GoodsReferencePriceQueryDto params);

	/**
	* 条件查询列表
	* @author miaoguoxin
	* @date 2021/2/2
	*/
	List<ReferenceSettingResponseDto> getListV2(GoodsReferencePriceQueryDto params);

}
