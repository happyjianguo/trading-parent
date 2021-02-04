package com.dili.trading.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.dili.assets.sdk.dto.CusCategoryDTO;
import com.dili.assets.sdk.dto.CusCategoryQuery;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.orders.domain.TradingBillType;
import com.dili.orders.dto.ReferencePriceSettingItemDto;
import com.dili.orders.dto.ReferencePriceSettingRequestDto;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.trading.dto.GoodsReferencePriceQueryDto;
import com.dili.trading.dto.ReferenceSettingResponseDto;
import com.dili.trading.rpc.GenericRpcResolver;
import com.dili.trading.rpc.GoodsReferencePriceSettingRpc;
import com.dili.trading.service.GoodsReferencePriceSettingService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description: 品类参考价接口实现GoodsReferencePriceSettingServiceImpl
 *
 * @date: 2020/8/21
 * @author: Seabert.Zhan
 */
@Service
public class GoodsReferencePriceSettingServiceImpl implements GoodsReferencePriceSettingService {

    @Autowired
    private GoodsReferencePriceSettingRpc goodsReferencePriceSettingRpc;

    @Autowired
    private AssetsRpc assetsRpc;

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "品类参考价新增/修改", operationType = "add", systemCode = "ORDERS")
    public void saveOrEdit(ReferencePriceSettingRequestDto requestDto) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<?> goodsReferencePriceSettingBaseOutput = goodsReferencePriceSettingRpc.saveOrEdit(requestDto);
        if (goodsReferencePriceSettingBaseOutput.isSuccess()) {
            // GoodsReferencePriceSetting data = goodsReferencePriceSettingBaseOutput.getData();
            //  LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
    }

    @Override
    public ReferenceSettingResponseDto getDetail(Long goodId) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        GoodsReferencePriceSetting queryParam = new GoodsReferencePriceSetting();
        queryParam.setMarketId(userTicket.getFirmId());
        queryParam.setGoodsId(goodId);
        List<GoodsReferencePriceSetting> settings = goodsReferencePriceSettingRpc.getAllGoods(queryParam);
        if (CollectionUtil.isEmpty(settings)) {
            CusCategoryDTO categoryDTO = GenericRpcResolver.resolver(assetsRpc.getCusCategory(goodId), "assert-service");
            return this.buildEmptySettingV2(categoryDTO);
        }
        return this.convert2SettingDto(settings);
    }

    @Override
    public List<ReferenceSettingResponseDto> getListByParentV2(GoodsReferencePriceQueryDto params) {
        CusCategoryQuery categoryDTO = new CusCategoryQuery();
        categoryDTO.setMarketId(params.getFirmId());
        categoryDTO.setParent(params.getParentGoodsId());
        categoryDTO.setState(EnabledStateEnum.ENABLED.getCode());
        //获取当前节点本身的数据
        CusCategoryDTO categoryDTOOneSelf = GenericRpcResolver.resolver(
                assetsRpc.getCusCategory(params.getParentGoodsId()), "assets-service");
        if (categoryDTOOneSelf == null) {
            throw new BusinessException(ResultCode.DATA_ERROR, "未获取到当前节点的商品数据");
        }
        //获取节点下面子节点的数据
        List<CusCategoryDTO> categoryDTOList = GenericRpcResolver.resolver(
                assetsRpc.listCusCategory(categoryDTO), "assets-service");
        List<CusCategoryDTO> categoryList = this.concatCategory2List(categoryDTOOneSelf, categoryDTOList);

        ////获取节点本身的数据（goods_reference_price_setting）
        GoodsReferencePriceSetting currentNodeParams = new GoodsReferencePriceSetting();
        currentNodeParams.setGoodsId(params.getParentGoodsId());
        currentNodeParams.setMarketId(params.getFirmId());
        List<GoodsReferencePriceSetting> curNodeSetting = goodsReferencePriceSettingRpc.getAllGoods(currentNodeParams);

        //获取节点子节点的数据（goods_reference_price_setting）
        GoodsReferencePriceSetting childrenNodeParams = new GoodsReferencePriceSetting();
        childrenNodeParams.setParentGoodsId(params.getParentGoodsId());
        childrenNodeParams.setMarketId(params.getFirmId());
        List<GoodsReferencePriceSetting> childrenNodeSettings = goodsReferencePriceSettingRpc.getAllGoods(childrenNodeParams);
        Map<Long, List<GoodsReferencePriceSetting>> listMap = this.concatSetting2MapList(curNodeSetting, childrenNodeSettings);

        return this.combineSettingListV2(params.getOnlyExistReferencePrice(), categoryList, listMap);
    }

    @Override
    public List<ReferenceSettingResponseDto> getListV2(GoodsReferencePriceQueryDto params) {
        CusCategoryQuery query = new CusCategoryQuery();
        query.setMarketId(params.getFirmId());
        query.setKeyword(params.getGoodsName());

        List<CusCategoryDTO> categoryList = GenericRpcResolver.resolver(
                assetsRpc.listCusCategory(query), "assets-service");

        GoodsReferencePriceSetting settingQuery = new GoodsReferencePriceSetting();
        settingQuery.setMarketId(params.getFirmId());
        List<GoodsReferencePriceSetting> referencePriceSettings = goodsReferencePriceSettingRpc.getAllGoods(settingQuery);
        Map<Long, List<GoodsReferencePriceSetting>> listMap = this.concatSetting2MapList(new ArrayList<>(), referencePriceSettings);
        return this.combineSettingListV2(params.getOnlyExistReferencePrice(), categoryList, listMap);
    }

    private ReferenceSettingResponseDto buildEmptySettingV2(CusCategoryDTO category) {
        ReferenceSettingResponseDto settingResponseDto = new ReferenceSettingResponseDto();
        settingResponseDto.setGoodsId(category.getId());
        settingResponseDto.setGoodsName(category.getName());
        settingResponseDto.setParentGoodsId(category.getParent());
        return settingResponseDto;
    }

    private Map<Long, List<GoodsReferencePriceSetting>> concatSetting2MapList(List<GoodsReferencePriceSetting> curNodeSetting,
                                                                              List<GoodsReferencePriceSetting> childrenNodeSettings) {
        //合并当前节点和子节点
        curNodeSetting.addAll(childrenNodeSettings);
        return curNodeSetting.stream().collect(Collectors.groupingBy(GoodsReferencePriceSetting::getGoodsId));
    }

    private List<CusCategoryDTO> concatCategory2List(CusCategoryDTO category, List<CusCategoryDTO> list) {
        List<CusCategoryDTO> categoryList = new ArrayList<>();
        if (category != null) {
            categoryList.add(category);
        }
        if (CollectionUtil.isNotEmpty(list)) {
            categoryList.addAll(list);
        }
        return categoryList;
    }

    private List<ReferenceSettingResponseDto> combineSettingListV2(Integer onlyFlag, List<CusCategoryDTO> categoryList, Map<Long, List<GoodsReferencePriceSetting>> listMap) {
        List<ReferenceSettingResponseDto> result = new ArrayList<>();
        for (CusCategoryDTO cusCategoryDTO : categoryList) {
            List<GoodsReferencePriceSetting> settings = listMap.get(cusCategoryDTO.getId());
            if (CollectionUtil.isEmpty(settings)) {
                //只查询有规则的项目
                if (onlyFlag != null && onlyFlag == 1) {
                    continue;
                }
                ReferenceSettingResponseDto responseDto = this.buildEmptySettingV2(cusCategoryDTO);
                result.add(responseDto);
            } else {
                ReferenceSettingResponseDto responseDto = this.convert2SettingDto(settings);
                result.add(responseDto);
            }
        }
        return result;
    }

    private ReferenceSettingResponseDto convert2SettingDto(List<GoodsReferencePriceSetting> settings) {
        ReferenceSettingResponseDto settingResponseDto = new ReferenceSettingResponseDto();
        for (GoodsReferencePriceSetting setting : settings) {
            settingResponseDto.setGoodsId(setting.getGoodsId());
            settingResponseDto.setGoodsName(setting.getGoodsName());
            settingResponseDto.setParentGoodsId(setting.getParentGoodsId());
            ReferencePriceSettingItemDto itemDto = new ReferencePriceSettingItemDto();
            itemDto.setTradeType(setting.getTradeType());
            itemDto.setCreatorId(setting.getCreatorId());
            itemDto.setModifierId(setting.getModifierId());
            itemDto.setFixedPrice(setting.getFixedPrice());
            itemDto.setReferenceRule(setting.getReferenceRule());
            if (TradingBillType.WEIGHING.getValue().equals(setting.getTradeType())) {
                settingResponseDto.setGenericItem(itemDto);
            } else if (TradingBillType.FARMER.getValue().equals(setting.getTradeType())) {
                settingResponseDto.setTraditionFarmerItem(itemDto);
            } else {
                settingResponseDto.setSelfItem(itemDto);
            }
        }
        return settingResponseDto;
    }
}
