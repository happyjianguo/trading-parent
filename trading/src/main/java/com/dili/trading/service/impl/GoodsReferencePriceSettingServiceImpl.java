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
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.trading.dto.GoodsReferencePriceQueryDto;
import com.dili.trading.rpc.GenericRpcResolver;
import com.dili.trading.rpc.GoodsReferencePriceSettingRpc;
import com.dili.trading.service.GoodsReferencePriceSettingService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 品类参考价接口实现GoodsReferencePriceSettingServiceImpl
 *
 * @date:    2020/8/21
 * @author:   Seabert.Zhan
 */
@Service
public class GoodsReferencePriceSettingServiceImpl implements GoodsReferencePriceSettingService {

    @Autowired
    private GoodsReferencePriceSettingRpc goodsReferencePriceSettingRpc;

    @Autowired
    private AssetsRpc assetsRpc;


    /**
     * 新增
     *
     * @param goodsReferencePriceSetting
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "品类参考价新增", operationType = "add", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public BaseOutput<GoodsReferencePriceSetting> insertGoodsReferencePriceSetting(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置创建人id
        goodsReferencePriceSetting.setCreatorId(userTicket.getId());
        //设置数据创建时间
        goodsReferencePriceSetting.setCreatedTime(LocalDateTime.now());
        //设置修改人id
        goodsReferencePriceSetting.setModifierId(userTicket.getId());
        //设置数据修改时间
        goodsReferencePriceSetting.setModifiedTime(LocalDateTime.now());

        BaseOutput<GoodsReferencePriceSetting> goodsReferencePriceSettingBaseOutput = goodsReferencePriceSettingRpc.insert(goodsReferencePriceSetting);
        if (goodsReferencePriceSettingBaseOutput.isSuccess()) {
            GoodsReferencePriceSetting data = goodsReferencePriceSettingBaseOutput.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return goodsReferencePriceSettingBaseOutput;
    }

    /**
     * 修改
     *
     * @param goodsReferencePriceSetting
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "品类参考价修改", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<GoodsReferencePriceSetting> updateGoodsReferencePriceSetting(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置修改人id
        goodsReferencePriceSetting.setModifierId(userTicket.getId());
        //设置数据修改时间
        goodsReferencePriceSetting.setModifiedTime(LocalDateTime.now());

        BaseOutput<GoodsReferencePriceSetting> goodsReferencePriceSettingBaseOutput = goodsReferencePriceSettingRpc.update(goodsReferencePriceSetting);
        if (goodsReferencePriceSettingBaseOutput.isSuccess()) {
            GoodsReferencePriceSetting data = goodsReferencePriceSettingBaseOutput.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return goodsReferencePriceSettingBaseOutput;
    }

    @Override
    public List<GoodsReferencePriceSetting> getListByParent(GoodsReferencePriceQueryDto params) {
        CusCategoryQuery categoryDTO = new CusCategoryQuery();
        categoryDTO.setMarketId(params.getFirmId());
        categoryDTO.setParent(params.getParentGoodsId());
        categoryDTO.setState(EnabledStateEnum.ENABLED.getCode());
        //获取当前节点本身的数据
        CusCategoryDTO categoryDTOOneSelf = GenericRpcResolver.resolver(
                assetsRpc.getCusCategory(params.getParentGoodsId()), "assets-service");
        if (categoryDTOOneSelf == null){
            throw new BusinessException(ResultCode.DATA_ERROR,"未获取到当前节点的商品数据");
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
        List<GoodsReferencePriceSetting> childrenNodeSettings =  goodsReferencePriceSettingRpc.getAllGoods(childrenNodeParams);
        Map<Long, GoodsReferencePriceSetting> referencePriceSettingMap = this.concatSetting2Map(curNodeSetting, childrenNodeSettings);

        return this.combineSettingList(params.getOnlyExistReferencePrice(), categoryList, referencePriceSettingMap);
    }


    @Override
    public List<GoodsReferencePriceSetting>  getList(GoodsReferencePriceQueryDto params) {
        CusCategoryQuery query = new CusCategoryQuery();
        query.setMarketId(params.getFirmId());
        query.setKeyword(params.getGoodsName());

        List<CusCategoryDTO> categoryList = GenericRpcResolver.resolver(
                assetsRpc.listCusCategory(query), "assets-service");

        GoodsReferencePriceSetting settingQuery = new GoodsReferencePriceSetting();
        settingQuery.setMarketId(params.getFirmId());
        List<GoodsReferencePriceSetting> referencePriceSettings = goodsReferencePriceSettingRpc.getAllGoods(settingQuery);
        Map<Long, GoodsReferencePriceSetting> referencePriceSettingMap = this.concatSetting2Map(new ArrayList<>(), referencePriceSettings);
        return this.combineSettingList(params.getOnlyExistReferencePrice(), categoryList, referencePriceSettingMap);
    }


    private GoodsReferencePriceSetting createEmptySetting(CusCategoryDTO category){
        GoodsReferencePriceSetting tempSetting = new GoodsReferencePriceSetting();
        tempSetting.setGoodsId(category.getId());
        tempSetting.setGoodsName(category.getName());
        tempSetting.setParentGoodsId(category.getParent());
        tempSetting.setReferenceRule(null);
        return tempSetting;
    }

    private Map<Long,GoodsReferencePriceSetting> concatSetting2Map(List<GoodsReferencePriceSetting> curNodeSetting,
                                                                   List<GoodsReferencePriceSetting> childrenNodeSettings){
        //合并当前节点和子节点
        curNodeSetting.addAll(childrenNodeSettings);
        return curNodeSetting.stream().collect(
                Collectors.toMap(GoodsReferencePriceSetting::getGoodsId, Function.identity(),
                        (key1, key2) -> key2, LinkedHashMap::new));
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

    private List<GoodsReferencePriceSetting> combineSettingList(Integer onlyFlag, List<CusCategoryDTO> categoryList, Map<Long, GoodsReferencePriceSetting> referencePriceSettingMap) {
        List<GoodsReferencePriceSetting> result = new ArrayList<>();
        for (CusCategoryDTO cusCategoryDTO : categoryList) {
            GoodsReferencePriceSetting setting = referencePriceSettingMap.get(cusCategoryDTO.getId());
            if (setting == null) {
                //只查询有规则的项目
                if (onlyFlag != null && onlyFlag == 1) {
                    continue;
                }
                setting = this.createEmptySetting(cusCategoryDTO);
            }
            result.add(setting);
        }
        return result;
    }

}
